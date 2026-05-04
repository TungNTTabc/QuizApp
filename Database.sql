-- Đưa về master để có thể xóa DB nếu đang được sử dụng
USE master;
GO

-- Nếu Database đã tồn tại thì xóa đi để làm lại từ đầu cho sạch
IF DB_ID('QuizAppDB') IS NOT NULL
BEGIN
    ALTER DATABASE QuizAppDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuizAppDB;
END
GO

-- Tạo mới Database
CREATE DATABASE QuizAppDB;
GO

-- Sử dụng Database vừa tạo
USE QuizAppDB;
GO

-- 1. Bảng Người dùng (Giáo viên & Học sinh)
-- Lưu trữ tài khoản, mật khẩu và thông tin cá nhân
CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    Role VARCHAR(10) NOT NULL, -- 'GV' cho Giáo viên, 'HS' cho Học sinh
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    FullName NVARCHAR(100) NOT NULL,
    DOB DATE NOT NULL, -- Ngày sinh
    Gender NVARCHAR(10), -- Giới tính
    StudentID VARCHAR(20), -- Mã số sinh viên (Giáo viên thì ghi '0')
    ClassName NVARCHAR(50), -- Lớp
    MainSubject NVARCHAR(50), -- Môn học trọng tâm
    Phone VARCHAR(20),
    Email VARCHAR(100),
    Address NVARCHAR(255)
);
GO

-- 2. Bảng Câu hỏi (Do giáo viên thêm)
CREATE TABLE Questions (
    QuestionID INT IDENTITY(1,1) PRIMARY KEY,
    Subject NVARCHAR(100) NOT NULL, -- Môn học (nhập tay, tự lưu để hiện thành danh sách sau)
    Content NVARCHAR(MAX) NOT NULL, -- Nội dung câu hỏi
    AnswerA NVARCHAR(255) NOT NULL,
    AnswerB NVARCHAR(255) NOT NULL,
    AnswerC NVARCHAR(255) NOT NULL,
    AnswerD NVARCHAR(255) NOT NULL,
    CorrectAnswer CHAR(1) NOT NULL -- Lưu 'A', 'B', 'C', hoặc 'D'
);
GO

-- 3. Bảng Bài thi (Do giáo viên tạo)
CREATE TABLE Exams (
    ExamID INT IDENTITY(1,1) PRIMARY KEY,
    TeacherID INT FOREIGN KEY REFERENCES Users(UserID), -- Ai là người tạo bài thi này
    Title NVARCHAR(200) NOT NULL, -- Chủ đề bài thi
    Subject NVARCHAR(100) NOT NULL, -- Môn học
    QuestionCount INT NOT NULL, -- Số lượng câu hỏi
    Duration INT NOT NULL -- Thời gian làm bài (tính bằng phút)
);
GO

-- 4. Bảng liên kết Bài thi - Câu hỏi
-- Dùng để biết "Bài thi A" bao gồm những "Câu hỏi" cụ thể nào
CREATE TABLE ExamQuestions (
    ExamID INT FOREIGN KEY REFERENCES Exams(ExamID),
    QuestionID INT FOREIGN KEY REFERENCES Questions(QuestionID),
    PRIMARY KEY (ExamID, QuestionID)
);
GO

-- 5. Bảng Lịch sử làm câu hỏi (Dành cho Học sinh)
-- Lưu lại: môn học, ngày/tháng làm, số câu đúng / tổng số câu, thời gian làm bài
CREATE TABLE PracticeHistory (
    HistoryID INT IDENTITY(1,1) PRIMARY KEY,
    StudentID INT FOREIGN KEY REFERENCES Users(UserID),
    Subject NVARCHAR(100),
    DateTaken DATETIME DEFAULT GETDATE(), -- Ngày giờ làm bài
    CorrectCount INT NOT NULL, -- Số câu đúng
    TotalCount INT NOT NULL, -- Tổng số câu
    DurationInSeconds INT NOT NULL -- Thời gian làm bài (lưu bằng giây cho chuẩn xác)
);
GO

-- 6. Bảng Kết quả Bài thi (Dành cho Học sinh)
-- Lưu lại: mã số sinh viên, họ tên (lấy từ UserID), số câu đúng / tổng số, thời gian
CREATE TABLE ExamResults (
    ResultID INT IDENTITY(1,1) PRIMARY KEY,
    ExamID INT FOREIGN KEY REFERENCES Exams(ExamID),
    StudentID INT FOREIGN KEY REFERENCES Users(UserID),
    CorrectCount INT NOT NULL,
    TotalCount INT NOT NULL,
    DurationInSeconds INT NOT NULL,
    DateTaken DATETIME DEFAULT GETDATE()
);
GO

-- 7. Bảng Ghi chú (Note) của Học sinh
CREATE TABLE StudentNotes (
    NoteID INT IDENTITY(1,1) PRIMARY KEY,
    StudentID INT FOREIGN KEY REFERENCES Users(UserID),
    NoteContent NVARCHAR(MAX)
);
GO

-- Chèn sẵn 1 tài khoản Giáo Viên để bạn có thể Đăng nhập ngay lập tức kiểm tra
INSERT INTO Users (Role, Username, Password, FullName, DOB, Gender, StudentID, ClassName, MainSubject, Phone, Email, Address)
VALUES ('GV', 'admin', 'admin', N'Giáo viên Admin', '1990-01-01', N'Nam', '0', N'Không', N'Toán', '0123456789', 'admin@quiz.com', N'Hà Nội');
GO

-- Chèn sẵn 1 tài khoản Học Sinh để test
INSERT INTO Users (Role, Username, Password, FullName, DOB, Gender, StudentID, ClassName, MainSubject, Phone, Email, Address)
VALUES ('HS', 'student1', '123456', N'Học sinh Test', '2005-05-05', N'Nữ', 'SV001', N'Lớp 10A1', N'Toán', '0987654321', 'hs@quiz.com', N'Hà Nội');
GO
