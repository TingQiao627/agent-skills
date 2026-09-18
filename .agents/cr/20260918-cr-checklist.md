# Code Review Checklist

> **Change** `hello-world (HelloWorld.java)` . **Branch/Commit** `AI/task-DEV-e7ff44d1-a774-11f1-8db0-a7bf3818e653-d01dc03f-f59d-4c85-9bf1-0b5b8911ee9e` / `2fb3f6a` . **Date** `2026-09-18`
>
> **AI**: Sole progress source; status only uses `⬜` `✅` `❌` `⚠️` `N/A`.
> **Completion Criteria**: All checkoff items must change from `⬜` to another status; `N/A` requires a reason.
>
> **Execution Order (mandatory)**: Before writing this checklist and entering per-file review, first run `references/script/scan-all-rules.sh` on the changed paths in the target repository, paste output into Step 3 and Step 4 notes; then use LLM to complete script-uncovered items and rechecks in Step 2-5.

---

## Step 1 — Execution Queue (Artifact A)

> **Step4 Column Semantics**: Each **Sn / Gn** represents "the scan conclusion for **this file** in Step 4 review against section **G*n*** of `reliability-checklist.md` and section **S*n*** of `security-checklist.md`". **Bug Patterns (B/M/I)** are not listed separately in this table; they are checked off by checklist ID in **§4.1** below (can be compared with `scan-all-rules.sh` pre-scan results). Fill `N/A` if unrelated to change; `✅` if scanned with no hit; `⚠️` or `❌` if risk hit (and write clear `Gx.x` / `Sx.x` + `path:line` in Step 4 detail table and report).

**Column Description (aligned with references sections)**

| Column Group | Column Name | Corresponding Checklist Section |
|------|------|----------------|
| Reliability | **G1** … **G17** (+ **G18** detail table only) | `reliability-checklist.md` — G1 Concurrency … G17 Emergency; **G18** security reinforcement checked off item-by-item in Step 4.2, need not be listed separately in Step 1 |
| Security | **S1** … **S10** | `security-checklist.md` — S1 SQL Injection … S10 CSRF/CORS/Redirect |

| # | File (repository relative path) | Ownership Reason | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | Overall Status |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | HelloWorld.java | REQ-1 / hello-world print | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ reviewed |

- Expanded from `git diff --name-only`; **glob prohibited**; non-Java marked `skip`.
- **Guard**: No `.java` -> terminate per skill. ✅ Java file present.
- **Closing**: After each file's **Sn/Gn** columns are all non-`⬜`, verify consistency with the Step 4 **item-by-item ID table** below.

---

## Step 2 — Functionality (Artifact B)

> Extract **REQ** only from spec/tasks; do not fabricate. Non-conformance to spec marked **P0**.
> Each REQ must fill in **spec evidence** and **associated file**; if P0 is hit, code evidence must include `path:line`, test, or interface behavior.

| REQ | Scenario | Spec Evidence (original text/section) | Associated File | Status | Code Evidence (file/test/interface) |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | Program prints "Hello, World!" to stdout | Requirement: "写个print hello world的程序" (write a print hello world program) | HelloWorld.java | ✅ | `HelloWorld.java:6` — `System.out.println("Hello, World!");` outputs the expected string |

---

## Step 3 — Readability Check (Artifact C)

> No Java: **entire section N/A**.

Check off section by section against `references/readability-checklist.md` A1-A7:

| ID | Check Item | Status | Notes (write `path:line` for hits) |
|----|--------|------|----------------------------|
| A1 | Source file format | ✅ | A1.1: File name matches class name `HelloWorld.java` ↔ `HelloWorld` ✅; A1.2: UTF-8 encoding ✅; A1.3: Uses spaces, no tabs ✅ |
| A2 | Source file structure/import order | ✅ | A2.1: No package/import needed for minimal class ✅; No wildcard imports; single top-level class ✅ |
| A3 | Code style | ✅ | A3.1: K&R braces ✅; A3.3: 4-space indent ✅; A3.4: Line width ≤120 ✅; A3.7: Space after keyword `public static void main` ✅ |
| A4 | Naming conventions | ✅ | A4.2: `HelloWorld` UpperCamelCase ✅; A4.3: `main` lowerCamelCase ✅ |
| A5 | Coding practices | ✅ | N/A — no overridden methods, no catch blocks, no static method call via instance |
| A6 | Specific element styles | ✅ | A6.1: `String[] args` — brackets on type ✅ |
| A7 | Javadoc conventions | ✅ | A7.1: public class has Javadoc comment ✅ |

---

## Step 4 — Reliability Check (Artifact D)

> **Item-by-item checkoff (mandatory)**: Each G/S ID **occupies its own row**; merging into ranges is prohibited. **Bug Patterns** are checked off per **each B*/M*/I*** ID row from `bug-pattern-checklist.md` (120 rules)**; IDs unrelated to the change can be marked `N/A` with a reason. Report severity: **Blocker->P0, Major->P1, Info->P2**.

### 4.1 Bug Patterns (`bug-pattern-checklist.md`)

> Pre-scan result: `scan-all-rules.sh` executed on `HelloWorld.java` — **No findings. 52/222 rules scanned.**

| ID | Status | Notes (write `path:line` for hits; pre-scan can paste script summary) |
|----|------|--------------------------------------------------|
| B001 | N/A | No `parse/of` calls with literals |
| B002 | N/A | No array `.equals()` usage |
| B003 | N/A | No `Arrays.fill` usage |
| B004 | N/A | No array `toString()` usage |
| B005 | N/A | No `Arrays.asList` with primitive array |
| B006 | N/A | No JUnit assertions |
| B007 | N/A | No exception catching |
| B008 | N/A | No `Executors` usage |
| B009 | N/A | No bit shift operations |
| B010 | N/A | No `BigDecimal` usage |
| B011 | N/A | No boxed type `==` comparison |
| B012 | N/A | No `Calendar` usage |
| B013 | N/A | No `Calendar` usage |
| B014 | N/A | No collection query operations |
| B015 | N/A | No `Collection.toArray()` usage |
| B016 | N/A | No `Comparable` implementation |
| B017 | N/A | No `this == null` check |
| B018 | N/A | No ternary with numeric promotion |
| B019 | N/A | No Money API usage |
| B020 | N/A | No compile-time constant multiplication |
| B021 | N/A | No Jedis usage |
| B022 | N/A | No `SimpleDateFormat` usage |
| B023 | N/A | No dead exception creation |
| B024 | N/A | No `Thread` usage |
| B025 | N/A | No double-brace initialization |
| B026 | N/A | No `x.equals(null)` |
| B027 | N/A | No `equals` override |
| B028 | N/A | No `DateUtil` usage |
| B029 | N/A | No setter assignment |
| B030 | N/A | No floating-point `==` |
| B031 | N/A | No `String.format` usage |
| B032 | N/A | No annotation `getClass()` |
| B033 | N/A | No `Unsafe` usage |
| B034 | N/A | No `Hashtable` usage |
| B035 | N/A | No identity binary expression |
| B036 | N/A | No `IdentityHashMap` usage |
| B037 | N/A | No varargs conditional |
| B038 | N/A | No recursion |
| B039 | N/A | No `indexOf` with reversed args |
| B040 | N/A | No `isInstance()` usage |
| B041 | N/A | No JDBC connection usage |
| B042 | N/A | No JUnit3 test |
| B043 | N/A | No JUnit inner class test |
| B044 | N/A | No JUnit3/4 mixed usage |
| B045 | N/A | No lock on boxed primitive |
| B046 | N/A | No loop conditions |
| B047 | N/A | No numeric compare methods |
| B048 | N/A | No `Math.round` with integer |
| B049 | N/A | No date formatting |
| B050 | N/A | No hour format usage |
| B051 | N/A | No `Boolean.getBoolean` |
| B052 | N/A | No week-year formatting |
| B053 | N/A | No test with missing `fail()` |
| B054 | N/A | No `EqualsTester` usage |
| B055 | N/A | No Mockito usage |
| B056 | N/A | No `Arrays.asList` modification |
| B057 | N/A | No collection modification in for-each |
| B058 | N/A | No collection self-modification |
| B059 | N/A | No `nCopies` with char |
| B060 | N/A | No ternary with null unboxing |
| B061 | N/A | No `sun.misc.BASE64Encoder` |
| B062 | N/A | No class loader cast |
| B063 | N/A | No `javax.xml` usage |
| B064 | N/A | No `Optional` equality |
| B065 | N/A | No Pojo self-assignment |
| B066 | N/A | No `Math.random()` cast |
| B067 | N/A | No `Random.nextInt()` modulo |
| B068 | N/A | No self-assignment |
| B069 | N/A | No `compareTo` with self |
| B070 | N/A | No `equals` with self |
| B071 | N/A | No `size() >= 0` check |
| B072 | N/A | No `Stream.toString()` |
| B073 | N/A | No `StringBuilder` with char |
| B074 | N/A | No `substring(0)` |
| B075 | N/A | No for loop conditions |
| B076 | N/A | No `@Transactional` usage |
| B077 | N/A | No `catch Throwable` in tests |
| B078 | N/A | No Truth `isEqualTo(self)` |
| B079 | N/A | No `@Mock` assignment |
| B080 | N/A | No unit test assertions |
| B081 | N/A | No collection sorted in-place unused |
| M001 | N/A | No consecutive identical checks |
| M002 | N/A | No `instanceof` misuse |
| M003 | N/A | No boxed constructor usage |
| M004 | N/A | No exception `printStackTrace` |
| M005 | N/A | No inner class that can be static |
| M006 | N/A | No compile-time boolean constant |
| M007 | N/A | No empty catch block |
| M008 | N/A | No `equals/hashCode` override |
| M009 | N/A | No incompatible type `equals` |
| M010 | N/A | No bitwise expression |
| M011 | N/A | No switch fall-through |
| M012 | N/A | No finally block |
| M013 | N/A | No float cast |
| M014 | N/A | No `getClass` on enum |
| M015 | N/A | No field hiding |
| M016 | N/A | No `LocalDateTime.now()` without TZ |
| M017 | N/A | No JUnit4 test missing `@Test` |
| M018 | N/A | No lock-not-before-try |
| M019 | N/A | No enum switch |
| M020 | N/A | No `@Override` missing |
| M021 | N/A | No `equals` not overriding |
| M022 | N/A | No `Optional.of(null)` |
| M023 | N/A | No `toString()` on POJO |
| M024 | N/A | No `Optional.get()` on empty |
| M025 | N/A | No protected members in final class |
| M026 | N/A | No `@Mock static` |
| M027 | N/A | No `ThreadLocal` usage |
| I001 | N/A | No exception assertion |
| I002 | N/A | No `@DoNotMock` usage |
| I003 | N/A | No `@AutoValue` usage |
| I004 | N/A | No `java.util.Date` usage |
| I005 | N/A | No JUnit4 class in JUnit3 |
| I006 | N/A | No `setUp` |
| I007 | N/A | No `tearDown` |
| I008 | N/A | No `@DataProvider` |
| I009 | N/A | No unit test method |
| I010 | N/A | No Spring runner |

### 4.2 Reliability (`reliability-checklist.md`)

| ID | Status | Notes |
|----|------|------|
| G1.1 | N/A | No concurrent scenario |
| G1.2 | N/A | No lock usage |
| G1.3 | N/A | No optimistic lock |
| G1.4 | N/A | No multi-resource locking |
| G2.1 | N/A | No write interface |
| G2.2 | N/A | No retry/schedule |
| G2.3 | N/A | No idempotency key |
| G3.1 | N/A | No distributed transaction |
| G3.2 | N/A | No `@Transactional` |
| G4.1 | N/A | No SQL |
| G4.2 | N/A | No SQL |
| G4.3 | N/A | No SQL |
| G5.1 | N/A | No MQ consumption |
| G6.1 | N/A | No caching |
| G6.2 | N/A | No caching |
| G7.1 | N/A | No scheduled task |
| G7.2 | N/A | No scheduled task |
| G8.1 | N/A | No exception handling |
| G8.2 | N/A | No strong dependency |
| G8.3 | N/A | No I/O stream |
| G8.4 | N/A | No thread pool |
| G8.5 | N/A | No ThreadLocal |
| G8.6 | N/A | No Executors usage |
| G9.1 | N/A | No RPC/HTTP call |
| G9.2 | N/A | No external call |
| G9.3 | N/A | No retry |
| G10.1 | N/A | No interface contract |
| G10.2 | N/A | No contract change |
| G11.1 | N/A | No unit test in this change |
| G11.2 | N/A | No boundary test |
| G11.3 | N/A | No input validation needed |
| G11.4 | N/A | No numeric operations |
| G12.1 | N/A | No financial scenario |
| G12.2 | N/A | No financial scenario |
| G13.1 | N/A | No logging |
| G14.1 | N/A | No amount usage |
| G14.2 | N/A | No multi-tenancy |
| G14.3 | N/A | No timezone storage |
| G14.4 | N/A | No date formatting |
| G15.1 | N/A | No DB structure change |
| G15.2 | N/A | No interface coexistence |
| G15.3 | N/A | No incompatible logic |
| G16.1 | N/A | No metric instrumentation needed |
| G16.2 | N/A | No exception path |
| G16.3 | N/A | No logging |
| G16.4 | N/A | No empty catch |
| G17.1 | N/A | No feature switch needed |
| G17.2 | N/A | No degradation needed |
| G17.3 | N/A | No rollback script needed |

### 4.3 Security (`security-checklist.md`)

| ID | Status | Notes |
|----|------|------|
| S1.1 | N/A | No SQL |
| S1.2 | N/A | No SQL |
| S1.3 | N/A | No SQL |
| S2.1 | N/A | No HTML/JS output |
| S2.2 | N/A | No rich text |
| S2.3 | N/A | No template engine |
| S3.1 | N/A | No URL request |
| S3.2 | N/A | No redirect |
| S3.3 | N/A | No timeout needed |
| S4.1 | N/A | No system command |
| S4.2 | N/A | No file operation |
| S5.1 | N/A | No XML parsing |
| S5.2 | N/A | No XPath usage |
| S6.1 | N/A | No deserialization |
| S6.2 | N/A | No JSON deserialization |
| S6.3 | N/A | No serialization |
| S7.1 | N/A | No file upload |
| S7.2 | N/A | No file download |
| S7.3 | N/A | No file storage |
| S8.1 | N/A | No authentication needed |
| S8.2 | N/A | No HTTP method |
| S8.3 | N/A | No data ID |
| S8.4 | N/A | No Cookie |
| S9.1 | N/A | No key/credential |
| S9.2 | N/A | No log with sensitive info |
| S9.3 | N/A | No transport encryption needed |
| S9.4 | N/A | No random number |
| S10.1 | N/A | No CSRF scenario |
| S10.2 | N/A | No CORS scenario |
| S10.3 | N/A | No redirect scenario |

---

## Step 5 — Custom Extension Check (Artifact E)

> Check off item-by-item per `customized-checklist.md`; if not enabled, entire section can be written as `N/A (custom rules not enabled)`.

### 5.1 Custom Extension (`customized-checklist.md`)

| ID | Status | Notes |
|----|------|------|
| U1.1 | N/A | Custom rules not enabled |
| U1.2 | N/A | Custom rules not enabled |
| U1.3 | N/A | Custom rules not enabled |
| U2.1 | N/A | Custom rules not enabled |
| U2.2 | N/A | Custom rules not enabled |
| U2.3 | N/A | Custom rules not enabled |

---

## Final Check (Anti-Omission)

- [x] Each file in the execution queue has `Step2`, `Step3`, **S1-S10 / G1-G17** columns all non-`⬜` (except skipped files);
- [x] Every REQ/Scenario in Step 2 is non-`⬜`
- [x] A1-A7 in Step 3 are all non-`⬜`
- [x] All **G/S** and **B001-B081 / M001-M027 / I001-I010** IDs in Step 4 are non-`⬜` (`N/A` allowed, with reason)
- [x] All U* IDs in Step 5 are non-`⬜` (`N/A (custom rules not enabled)` allowed)
- [x] All `❌/⚠️` are written into report, including `ID + path:line`