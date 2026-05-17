# Bộ phân tích Codeforces

Ứng dụng Java 17 dạng Maven, giao diện Java Swing, dùng MySQL để lưu dữ liệu, Selenium để thu thập bài nộp và mã nguồn, Groq API để phân tích thuật toán, cấu trúc dữ liệu và mức nghi ngờ mã nguồn có AI hỗ trợ.

## Kiến trúc

Project đi theo mô hình MVC:

- `model`: lớp dữ liệu như `User`, `Submission`, `AnalysisResult`, `CfAccount`.
- `view`: toàn bộ giao diện Swing.
- `controller`: lớp trung gian giữa giao diện và nghiệp vụ.
- `service`: nghiệp vụ quản lý nick, tài khoản, thống kê, phân tích.
- `dao`: thao tác JDBC với MySQL.
- `database`: kết nối cơ sở dữ liệu.
- `crawler`: Selenium, đăng nhập Codeforces, quản lý phiên, thu thập bài nộp và mã nguồn.
- `analyzer`: gọi Groq API, tạo prompt, đọc JSON trả về, phân tích dự phòng.
- `scheduler`: lịch thu thập tự động mỗi 24 giờ.
- `utils`: cấu hình, thời gian, JSON, mã hóa mật khẩu.

## Cấu trúc thư mục

```text
codeforces-analyzer
├── pom.xml
├── README.md
├── sql
│   ├── schema.sql
│   └── sample-data.sql
└── src/main
    ├── java/com/codeforces/analyzer
    │   ├── Main.java
    │   ├── analyzer
    │   ├── controller
    │   ├── crawler
    │   ├── dao
    │   ├── database
    │   ├── model
    │   ├── scheduler
    │   ├── service
    │   ├── utils
    │   └── view
    └── resources/application.properties
```

## Cơ sở dữ liệu

Tên cơ sở dữ liệu: `codeforces_analyzer`.

Các bảng chính:

- `users`: lưu nick và nền tảng.
- `submissions`: lưu mã bài nộp, bài làm, kết quả, ngôn ngữ, thời gian nộp, mã nguồn và trạng thái thu thập.
- `analysis_results`: lưu thuật toán, cấu trúc dữ liệu, độ khó, xác suất nghi ngờ AI, lý do và điểm số.
- `crawl_logs`: lưu nhật ký thu thập.
- `cf_accounts`: lưu tài khoản Codeforces, mật khẩu đã mã hóa và dữ liệu phiên.

File tạo bảng đầy đủ nằm ở `sql/schema.sql`. File dữ liệu mẫu nằm ở `sql/sample-data.sql`.

## Cài MySQL

1. Cài MySQL Server 8 hoặc mới hơn.
2. Mở MySQL Workbench hoặc terminal MySQL.
3. Chạy file:

```sql
SOURCE C:/Users/anhar/OneDrive/Documents/codeforces-analyzer/sql/schema.sql;
```

4. Nếu muốn có dữ liệu mẫu:

```sql
SOURCE C:/Users/anhar/OneDrive/Documents/codeforces-analyzer/sql/sample-data.sql;
```

## Cấu hình ứng dụng

File mặc định: `src/main/resources/application.properties`.

Có thể tạo file riêng `config/application.properties` ở thư mục gốc project để ghi đè cấu hình mà không sửa file trong source.

Cấu hình MySQL:

```properties
db.url=jdbc:mysql://localhost:3306/codeforces_analyzer?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useSSL=false
db.username=root
db.password=mat_khau_mysql_cua_ban
```

Cấu hình Groq:

```properties
groq.api.key=khoa_groq_cua_ban
groq.model=llama-3.3-70b-versatile
```

Nếu chưa nhập khóa Groq, ứng dụng vẫn chạy được nhờ bộ phân tích dự phòng, nhưng kết quả chỉ dùng để tham khảo.

Cấu hình giới hạn thu thập:

```properties
crawler.max.pages=3
crawler.max.submissions=10
```

Với cấu hình trên, mỗi lần thu thập một nick, hệ thống chỉ xử lý tối đa 10 bài nộp mới nhất. `crawler.max.pages` vẫn được giữ như giới hạn phụ để tránh mở quá nhiều trang.

## Cài Selenium và ChromeDriver

Dependency Selenium đã có trong `pom.xml`. Bạn chỉ cần chuẩn bị Chrome và ChromeDriver đúng phiên bản yêu cầu:

- Google Chrome: `148.0.7778.168`
- ChromeDriver: `148.0.7778.167`

Cách đặt ChromeDriver:

1. Tạo thư mục `drivers` trong project.
2. Đặt file `chromedriver.exe` vào:

```text
C:/Users/anhar/OneDrive/Documents/codeforces-analyzer/drivers/chromedriver.exe
```

3. Kiểm tra cấu hình:

```properties
selenium.chromedriver.path=drivers/chromedriver.exe
selenium.expected.chrome.version=148.0.7778.168
selenium.expected.chromedriver.version=148.0.7778.167
selenium.headless=false
```

Nên để `selenium.headless=false` vì Codeforces có thể yêu cầu mã xác minh. Khi gặp mã xác minh, Selenium mở Chrome và chờ bạn xử lý thủ công trong thời gian cấu hình bởi `selenium.manual.captcha.seconds`.

## Import vào Eclipse

1. Mở Eclipse.
2. Chọn `File` → `Import`.
3. Chọn `Maven` → `Existing Maven Projects`.
4. Chọn thư mục:

```text
C:/Users/anhar/OneDrive/Documents/codeforces-analyzer
```

5. Bấm `Finish`.
6. Bấm chuột phải project → `Maven` → `Update Project`.
7. Chạy class:

```text
com.codeforces.analyzer.Main
```

## Chạy bằng Maven

Build:

```powershell
mvn clean package
```

Chạy giao diện:

```powershell
mvn exec:java
```

## Luồng hoạt động

1. Người dùng thêm nick Codeforces hoặc VJudge ở tab `Quản lý nick`.
2. Người dùng thêm tài khoản Codeforces ở tab `Tài khoản Codeforces`.
3. Ứng dụng mã hóa mật khẩu trước khi lưu vào bảng `cf_accounts`.
4. Selenium mở Chrome, đăng nhập Codeforces, lưu dữ liệu phiên để lần sau không cần đăng nhập lại.
5. Tab `Xem bài nộp` cho phép thu thập ngay danh sách bài nộp.
6. Với mỗi bài nộp mới, hệ thống vào trang bài nộp để lấy mã nguồn.
7. Nếu không xem được mã nguồn, hệ thống lưu trạng thái `Thất bại` và ghi nhật ký vào `crawl_logs`.
8. Tab `Phân tích AI` gửi mã nguồn sang Groq API và yêu cầu trả JSON.
9. Kết quả phân tích được lưu vào bảng `analysis_results`.
10. Tab `Thống kê` tổng hợp kỹ năng CTDL, thuật toán, tỉ lệ được chấp nhận, ngôn ngữ thường dùng và mức nghi ngờ AI.

## Chức năng kiểm tra

Trong tab `Tổng quan` có các nút:

- `Kiểm tra MySQL`: kiểm tra kết nối cơ sở dữ liệu.
- `Kiểm tra Groq`: kiểm tra khóa Groq API.
- `Bật thu thập mỗi 24 giờ`: bật lịch tự động.
- `Thu thập tất cả ngay`: thu thập tất cả nick đã lưu.

Trong tab `Tài khoản Codeforces` có các nút:

- `Đăng nhập`: mở Chrome và đăng nhập Codeforces.
- `Kiểm tra đăng nhập`: kiểm tra phiên hiện tại.
- `Đăng xuất`: xóa dữ liệu phiên đăng nhập.

## Các class chính

- `DatabaseConnection.java`: tạo kết nối JDBC và kiểm tra MySQL.
- `UserDAO.java`: thêm, sửa, xóa, tìm kiếm nick.
- `SubmissionDAO.java`: lưu bài nộp mới, cập nhật mã nguồn, truy vấn thống kê.
- `AnalysisDAO.java`: lưu kết quả phân tích AI.
- `CodeforcesLoginService.java`: đăng nhập Codeforces bằng Selenium.
- `CookieManager.java`: lưu và nạp dữ liệu phiên.
- `SessionManager.java`: cấu hình ChromeDriver và Chrome.
- `SeleniumCrawler.java`: thu thập bài nộp và mã nguồn.
- `GroqService.java`: gọi Groq API.
- `GroqAnalyzer.java`: tạo prompt và đọc JSON phân tích.
- `CrawlScheduler.java`: chạy thu thập tự động.
- `MainFrame.java`: cửa sổ Swing chính.

## Ghi chú thực tế

- Codeforces có thể thay đổi HTML, khi đó selector Selenium cần được cập nhật.
- Một số bài nộp Codeforces yêu cầu đăng nhập mới xem được mã nguồn.
- VJudge có thể giới hạn quyền xem mã nguồn công khai, nên phần VJudge chỉ mở và kiểm tra trang người dùng khi không có quyền đăng nhập riêng.
- Mật khẩu được mã hóa bằng AES/GCM. Hãy đổi `security.secret` trước khi dùng thật.
- Mức nghi ngờ AI là ước lượng hỗ trợ đánh giá, không phải kết luận tuyệt đối.
