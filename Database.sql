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

-- 2. Bảng Môn học (Chuẩn hóa)
CREATE TABLE Subjects (
    SubjectID INT IDENTITY(1,1) PRIMARY KEY,
    SubjectName NVARCHAR(100) UNIQUE NOT NULL
);
GO

-- 3. Bảng Câu hỏi (Do giáo viên thêm)
CREATE TABLE Questions (
    QuestionID INT IDENTITY(1,1) PRIMARY KEY,
    SubjectID INT FOREIGN KEY REFERENCES Subjects(SubjectID),
    Content NVARCHAR(MAX) NOT NULL, -- Nội dung câu hỏi
    AnswerA NVARCHAR(255) NOT NULL,
    AnswerB NVARCHAR(255) NOT NULL,
    AnswerC NVARCHAR(255) NOT NULL,
    AnswerD NVARCHAR(255) NOT NULL,
    CorrectAnswer CHAR(1) NOT NULL -- Lưu 'A', 'B', 'C', hoặc 'D'
);
GO

-- 4. Bảng Bài thi (Do giáo viên tạo)
CREATE TABLE Exams (
    ExamID INT IDENTITY(1,1) PRIMARY KEY,
    TeacherID INT FOREIGN KEY REFERENCES Users(UserID), -- Ai là người tạo bài thi này
    Title NVARCHAR(200) NOT NULL, -- Chủ đề bài thi
    SubjectID INT FOREIGN KEY REFERENCES Subjects(SubjectID),
    QuestionCount INT NOT NULL, -- Số lượng câu hỏi
    Duration INT NOT NULL -- Thời gian làm bài (tính bằng phút)
);
GO

-- 5. Bảng liên kết Bài thi - Câu hỏi
-- Dùng để biết "Bài thi A" bao gồm những "Câu hỏi" cụ thể nào
CREATE TABLE ExamQuestions (
    ExamID INT FOREIGN KEY REFERENCES Exams(ExamID) ON DELETE CASCADE,
    QuestionID INT FOREIGN KEY REFERENCES Questions(QuestionID) ON DELETE CASCADE,
    PRIMARY KEY (ExamID, QuestionID)
);
GO

-- 6. Bảng Kết quả (Gộp chung Lịch sử làm bài thi & luyện tập)
CREATE TABLE QuizResults (
    ResultID INT IDENTITY(1,1) PRIMARY KEY,
    StudentID INT FOREIGN KEY REFERENCES Users(UserID) ON DELETE CASCADE,
    ExamID INT FOREIGN KEY REFERENCES Exams(ExamID) ON DELETE CASCADE, -- NULL nếu là bài luyện tập tự do
    SubjectID INT FOREIGN KEY REFERENCES Subjects(SubjectID),
    ResultType VARCHAR(20) NOT NULL, -- 'EXAM' hoặc 'PRACTICE'
    CorrectCount INT NOT NULL,
    TotalCount INT NOT NULL,
    DurationInSeconds INT NOT NULL,
    DateTaken DATETIME DEFAULT GETDATE()
);
GO

-- 7. Bảng Chi tiết Lần làm bài (Lưu trữ đáp án của từng câu hỏi)
CREATE TABLE QuizAttemptDetails (
    AttemptDetailID INT IDENTITY(1,1) PRIMARY KEY,
    ResultID INT FOREIGN KEY REFERENCES QuizResults(ResultID) ON DELETE CASCADE,
    QuestionID INT FOREIGN KEY REFERENCES Questions(QuestionID),
    SelectedAnswer CHAR(1), -- Có thể NULL nếu bỏ trống
    IsCorrect BIT NOT NULL -- 1 nếu đúng, 0 nếu sai
);
GO

-- 8. Bảng Ghi chú (Note) của Học sinh
CREATE TABLE StudentNotes (
    NoteID INT IDENTITY(1,1) PRIMARY KEY,
    StudentID INT FOREIGN KEY REFERENCES Users(UserID) ON DELETE CASCADE,
    NoteContent NVARCHAR(MAX)
);
GO

-- TẠO CÁC CHỈ MỤC (INDEXES) ĐỂ TỐI ƯU HÓA TRUY VẤN
CREATE UNIQUE NONCLUSTERED INDEX UQ_Users_StudentID ON Users(StudentID) WHERE StudentID IS NOT NULL;
CREATE NONCLUSTERED INDEX IX_Users_Username ON Users(Username);
CREATE NONCLUSTERED INDEX IX_Questions_SubjectID ON Questions(SubjectID);
CREATE NONCLUSTERED INDEX IX_Exams_TeacherID ON Exams(TeacherID);
CREATE NONCLUSTERED INDEX IX_Exams_SubjectID ON Exams(SubjectID);
CREATE NONCLUSTERED INDEX IX_QuizResults_StudentID ON QuizResults(StudentID);
CREATE NONCLUSTERED INDEX IX_QuizResults_ExamID ON QuizResults(ExamID);
CREATE NONCLUSTERED INDEX IX_QuizAttemptDetails_ResultID ON QuizAttemptDetails(ResultID);
GO

-- Chèn sẵn 1 tài khoản Admin để đăng nhập và quản lý hệ thống
-- Mật khẩu ban đầu là "admin" đã được mã hóa SHA-256
INSERT INTO Users (Role, Username, Password, FullName, DOB, Gender, StudentID, ClassName, MainSubject, Phone, Email, Address)
VALUES ('ADMIN', 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', N'Admin Hệ Thống', '1990-01-01', N'Nam', NULL, N'Không', N'Toán', '0123456789', 'admin@quiz.com', N'Hà Nội');
GO

-- Chèn sẵn 1 tài khoản Học Sinh để test
-- Mật khẩu ban đầu là "123456" đã được mã hóa SHA-256
INSERT INTO Users (Role, Username, Password, FullName, DOB, Gender, StudentID, ClassName, MainSubject, Phone, Email, Address)
VALUES ('HS', 'student1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Học sinh Test', '2005-05-05', N'Nữ', 'SV001', N'Lớp 10A1', NULL, '0987654321', 'hs@quiz.com', N'Hà Nội');
GO
