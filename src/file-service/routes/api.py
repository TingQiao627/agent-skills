"""API 路由"""
from flask import Flask, request, jsonify, send_file
from werkzeug.utils import secure_filename
import os

from ..config import Config
from ..services.file_storage import FileStorage
from ..utils import get_file_extension, calculate_file_hash, calculate_chunk_hash

# 创建 Flask 应用
app = Flask(__name__)

# 初始化服务
config = Config()
storage = FileStorage(config)


@app.route('/api/files/upload/init', methods=['POST'])
def upload_init():
    """初始化上传"""
    data = request.get_json()
    
    file_name = data.get('file_name')
    file_size = data.get('file_size')
    file_type = data.get('file_type')
    file_hash = data.get('file_hash')
    uploader = data.get('uploader', 'anonymous')
    
    # 参数校验
    if not all([file_name, file_size, file_type, file_hash]):
        return jsonify({"error": "缺少必要参数"}), 400
    
    # 文件大小校验
    if file_size > config.MAX_FILE_SIZE:
        return jsonify({"error": f"文件大小超过限制 ({config.MAX_FILE_SIZE // (1024*1024)}MB)"}), 400
    
    # 文件类型校验
    ext = get_file_extension(file_name)
    if ext not in config.ALLOWED_EXTENSIONS:
        return jsonify({"error": f"不允许的文件类型: {ext}"}), 400
    
    # 初始化上传
    result = storage.init_upload(file_name, file_size, file_type, file_hash, uploader)
    return jsonify(result)


@app.route('/api/files/upload/chunk', methods=['POST'])
def upload_chunk():
    """上传分片"""
    upload_id = request.form.get('upload_id')
    chunk_index = int(request.form.get('chunk_index', 0))
    chunk_hash = request.form.get('chunk_hash', '')
    
    if 'chunk' not in request.files:
        return jsonify({"error": "缺少分片数据"}), 400
    
    chunk_file = request.files['chunk']
    chunk_data = chunk_file.read()
    
    # 校验分片哈希
    actual_hash = calculate_chunk_hash(chunk_data)
    if chunk_hash and chunk_hash != actual_hash:
        return jsonify({"error": "分片哈希校验失败"}), 400
    
    # 上传分片
    result = storage.upload_chunk(upload_id, chunk_index, chunk_data, actual_hash)
    
    if result.get('success'):
        return jsonify(result)
    else:
        return jsonify(result), 400


@app.route('/api/files/upload/merge', methods=['POST'])
def upload_merge():
    """合并分片"""
    data = request.get_json()
    upload_id = data.get('upload_id')
    
    if not upload_id:
        return jsonify({"error": "缺少 upload_id"}), 400
    
    result = storage.merge_chunks(upload_id)
    
    if result.get('success'):
        return jsonify(result)
    else:
        return jsonify(result), 400


@app.route('/api/files/upload/cancel', methods=['POST'])
def upload_cancel():
    """取消上传"""
    data = request.get_json()
    upload_id = data.get('upload_id')
    
    if not upload_id:
        return jsonify({"error": "缺少 upload_id"}), 400
    
    result = storage.cancel_upload(upload_id)
    
    if result.get('success'):
        return jsonify(result)
    else:
        return jsonify(result), 400


@app.route('/api/files/upload/check', methods=['POST'])
def upload_check():
    """检查文件是否存在（秒传）"""
    data = request.get_json()
    file_hash = data.get('file_hash')
    
    if not file_hash:
        return jsonify({"error": "缺少 file_hash"}), 400
    
    result = storage.check_file_exists(file_hash)
    return jsonify(result)


@app.route('/api/files/download/<file_id>', methods=['GET'])
def download_file(file_id):
    """下载文件"""
    download_info = storage.get_download_info(file_id)
    
    if not download_info:
        return jsonify({"error": "文件不存在"}), 404
    
    # 检查文件是否存在
    file_path = download_info['path']
    if not os.path.exists(file_path):
        return jsonify({"error": "文件已丢失"}), 404
    
    return send_file(
        file_path,
        as_attachment=True,
        download_name=download_info['name']
    )


@app.route('/api/files/download/batch', methods=['POST'])
def download_batch():
    """批量下载"""
    import zipfile
    import tempfile
    
    data = request.get_json()
    file_ids = data.get('file_ids', [])
    
    if not file_ids:
        return jsonify({"error": "缺少文件ID列表"}), 400
    
    # 数量限制
    if len(file_ids) > config.MAX_BATCH_DOWNLOAD_COUNT:
        return jsonify({"error": f"批量下载数量超过限制 ({config.MAX_BATCH_DOWNLOAD_COUNT})"}), 400
    
    # 检查文件并计算总大小
    total_size = 0
    files_to_download = []
    
    for file_id in file_ids:
        download_info = storage.get_download_info(file_id)
        if download_info:
            total_size += download_info['size']
            files_to_download.append(download_info)
    
    # 大小限制
    if total_size > config.MAX_BATCH_DOWNLOAD_SIZE:
        return jsonify({"error": f"批量下载总大小超过限制"}), 400
    
    if not files_to_download:
        return jsonify({"error": "没有可下载的文件"}), 400
    
    # 创建 ZIP 文件
    temp_zip = tempfile.NamedTemporaryFile(suffix='.zip', delete=False)
    
    try:
        with zipfile.ZipFile(temp_zip.name, 'w', zipfile.ZIP_DEFLATED) as zf:
            for file_info in files_to_download:
                if os.path.exists(file_info['path']):
                    zf.write(file_info['path'], file_info['name'])
        
        return send_file(
            temp_zip.name,
            as_attachment=True,
            download_name='files.zip',
            mimetype='application/zip'
        )
    finally:
        # 注意：在实际生产中应该使用更安全的临时文件清理方式
        pass


@app.route('/api/files/list', methods=['GET'])
def list_files():
    """获取文件列表"""
    page = int(request.args.get('page', 1))
    page_size = int(request.args.get('page_size', 20))
    search = request.args.get('search')
    sort_by = request.args.get('sort_by', 'created_at')
    sort_order = request.args.get('sort_order', 'desc')
    
    result = storage.list_files(page, page_size, search, sort_by, sort_order)
    return jsonify(result)


@app.route('/api/files/delete', methods=['POST'])
def delete_files():
    """删除文件"""
    data = request.get_json()
    file_ids = data.get('file_ids', [])
    
    if not file_ids:
        return jsonify({"error": "缺少文件ID列表"}), 400
    
    result = storage.delete_files(file_ids)
    return jsonify(result)


@app.route('/api/files/preview/<file_id>', methods=['GET'])
def preview_file(file_id):
    """文件预览"""
    file = storage.get_file(file_id)
    
    if not file:
        return jsonify({"error": "文件不存在"}), 404
    
    download_info = storage.get_download_info(file_id)
    file_path = download_info['path']
    
    if not os.path.exists(file_path):
        return jsonify({"error": "文件已丢失"}), 404
    
    # 获取文件类型
    file_type = file.type
    
    # 图片预览
    if file_type.startswith('image/'):
        return send_file(file_path, mimetype=file_type)
    
    # PDF 预览
    if file_type == 'application/pdf':
        return send_file(file_path, mimetype='application/pdf')
    
    # 文本预览
    if file_type.startswith('text/') or file_type == 'application/json':
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read(10240)  # 限制预览大小
            return jsonify({"type": "text", "content": content})
        except:
            return jsonify({"error": "无法读取文件内容"}), 500
    
    # 其他格式返回文件信息
    return jsonify({
        "type": "download",
        "file": file.to_dict(),
        "message": "不支持在线预览，请下载查看"
    })


if __name__ == '__main__':
    app.run(debug=True, port=5000)