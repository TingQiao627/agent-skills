package com.dt.example.hello.api.response;

/**
 * 问候视图对象。
 *
 * <p>封装问候语返回给前端。</p>
 */
public class HelloVO {

    /** 问候语内容 */
    private String message;

    /**
     * 创建携带指定消息的视图对象。
     *
     * @param message 问候语
     * @return HelloVO 实例
     */
    public static HelloVO of(String message) {
        HelloVO vo = new HelloVO();
        vo.setMessage(message);
        return vo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "HelloVO{message='" + message + "'}";
    }
}