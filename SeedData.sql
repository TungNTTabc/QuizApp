USE QuizAppDB;
GO

-- Xóa dữ liệu cũ nếu chạy lại kịch bản
DELETE FROM QuizAttemptDetails;
DELETE FROM QuizResults;
DELETE FROM ExamQuestions;
DELETE FROM Exams;
DELETE FROM Questions;
DELETE FROM Subjects;

-- Reset IDENTITY
DBCC CHECKIDENT ('Subjects', RESEED, 0);
DBCC CHECKIDENT ('Questions', RESEED, 0);
DBCC CHECKIDENT ('Exams', RESEED, 0);
DBCC CHECKIDENT ('QuizResults', RESEED, 0);
DBCC CHECKIDENT ('QuizAttemptDetails', RESEED, 0);
GO

-- 1. Chèn Môn học
INSERT INTO Subjects (SubjectName) VALUES 
(N'Toán'), 
(N'Vật lý'), 
(N'Hóa học'),
(N'Tiếng Anh'),
(N'Lịch sử'),
(N'Sinh học'),
(N'Địa lý'),
(N'Lập trình');
GO

-- Lấy ID Môn học
DECLARE @ToanID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Toán');
DECLARE @LyID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Vật lý');
DECLARE @HoaID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Hóa học');
DECLARE @AnhID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Tiếng Anh');
DECLARE @SuID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Lịch sử');
DECLARE @SinhID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Sinh học');
DECLARE @DiaID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Địa lý');
DECLARE @CodeID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Lập trình');

-- 2. Chèn Câu hỏi

-- Môn Toán
INSERT INTO Questions (SubjectID, Content, AnswerA, AnswerB, AnswerC, AnswerD, CorrectAnswer) VALUES
(@ToanID, N'1 + 1 bằng mấy?', N'1', N'2', N'3', N'4', 'B'),
(@ToanID, N'Căn bậc hai của 16 là?', N'2', N'4', N'8', N'16', 'B'),
(@ToanID, N'Nghiệm của phương trình 2x - 4 = 0 là?', N'x = 1', N'x = 2', N'x = 3', N'x = 4', 'B'),
(@ToanID, N'Đạo hàm của x^2 là gì?', N'x', N'2x', N'x^3', N'2', 'B'),
(@ToanID, N'Tích phân của 2x là?', N'x^2', N'x', N'2x^2', N'x^2 + C', 'D'),
(@ToanID, N'15% của 200 là bao nhiêu?', N'15', N'20', N'30', N'45', 'C'),
(@ToanID, N'Sin(90 độ) bằng bao nhiêu?', N'0', N'0.5', N'1', N'-1', 'C'),
(@ToanID, N'Log cơ số 10 của 1000 là?', N'1', N'2', N'3', N'10', 'C');

-- Môn Vật lý
INSERT INTO Questions (SubjectID, Content, AnswerA, AnswerB, AnswerC, AnswerD, CorrectAnswer) VALUES
(@LyID, N'Công thức tính lực?', N'F = m*a', N'F = m/a', N'F = v/t', N'F = m*v', 'A'),
(@LyID, N'Đơn vị của dòng điện là gì?', N'Vôn (V)', N'Oát (W)', N'Ampe (A)', N'Joule (J)', 'C'),
(@LyID, N'Gia tốc trọng trường trên trái đất xấp xỉ bao nhiêu?', N'9.8 m/s^2', N'10.5 m/s^2', N'8.9 m/s^2', N'11.2 m/s^2', 'A'),
(@LyID, N'Ánh sáng truyền đi trong chân không với tốc độ xấp xỉ bao nhiêu?', N'300,000 km/h', N'300,000 km/s', N'3,000 km/s', N'30,000 km/s', 'B'),
(@LyID, N'Ai là người phát minh ra bóng đèn sợi đốt?', N'Albert Einstein', N'Nikola Tesla', N'Thomas Edison', N'Isaac Newton', 'C');

-- Môn Hóa học
INSERT INTO Questions (SubjectID, Content, AnswerA, AnswerB, AnswerC, AnswerD, CorrectAnswer) VALUES
(@HoaID, N'Ký hiệu hóa học của Vàng là gì?', N'Ag', N'Au', N'Fe', N'Cu', 'B'),
(@HoaID, N'Công thức hóa học của nước là gì?', N'HO2', N'H2O', N'CO2', N'O2', 'B'),
(@HoaID, N'Axit sunfuric có công thức là gì?', N'HCl', N'HNO3', N'H2SO4', N'H3PO4', 'C'),
(@HoaID, N'Khí nào chiếm tỷ lệ lớn nhất trong khí quyển Trái Đất?', N'Oxy', N'Carbon dioxide', N'Hydro', N'Nitơ', 'D');

-- Môn Tiếng Anh
INSERT INTO Questions (SubjectID, Content, AnswerA, AnswerB, AnswerC, AnswerD, CorrectAnswer) VALUES
(@AnhID, N'Chọn từ đúng: She ____ to school every day.', N'go', N'goes', N'going', N'gone', 'B'),
(@AnhID, N'Trái nghĩa với "Happy" là?', N'Sad', N'Joy', N'Angry', N'Cry', 'A'),
(@AnhID, N'I have been learning English ____ 5 years.', N'since', N'for', N'in', N'at', 'B'),
(@AnhID, N'What is the past tense of "Go"?', N'Goes', N'Going', N'Gone', N'Went', 'D'),
(@AnhID, N'If it rains, we ____ stay at home.', N'will', N'would', N'are', N'have', 'A');

-- Môn Lịch sử
INSERT INTO Questions (SubjectID, Content, AnswerA, AnswerB, AnswerC, AnswerD, CorrectAnswer) VALUES
(@SuID, N'Chiến tranh thế giới thứ 2 kết thúc vào năm nào?', N'1940', N'1945', N'1950', N'1954', 'B'),
(@SuID, N'Chiến dịch Điện Biên Phủ kết thúc thắng lợi vào ngày tháng năm nào?', N'30/4/1975', N'2/9/1945', N'7/5/1954', N'19/8/1945', 'C'),
(@SuID, N'Vị vua cuối cùng của triều đại phong kiến Việt Nam là ai?', N'Khải Định', N'Hàm Nghi', N'Bảo Đại', N'Duy Tân', 'C'),
(@SuID, N'Nhà Trần đã ba lần đánh thắng quân xâm lược nào?', N'Quân Minh', N'Quân Mông - Nguyên', N'Quân Thanh', N'Quân Tống', 'B');

-- Môn Sinh học
INSERT INTO Questions (SubjectID, Content, AnswerA, AnswerB, AnswerC, AnswerD, CorrectAnswer) VALUES
(@SinhID, N'Thực vật thực hiện quá trình quang hợp vào lúc nào?', N'Ban đêm', N'Có ánh sáng', N'Lúc trời mưa', N'Bất cứ lúc nào', 'B'),
(@SinhID, N'Bộ phận nào của cây làm nhiệm vụ hút nước và muối khoáng?', N'Lá', N'Thân', N'Rễ', N'Hoa', 'C'),
(@SinhID, N'Bạch cầu có vai trò gì trong cơ thể người?', N'Vận chuyển oxy', N'Đông máu', N'Bảo vệ cơ thể', N'Hấp thụ dinh dưỡng', 'C'),
(@SinhID, N'Nhiễm sắc thể ở người bình thường có bao nhiêu chiếc?', N'23', N'46', N'48', N'44', 'B');

-- Môn Địa lý
INSERT INTO Questions (SubjectID, Content, AnswerA, AnswerB, AnswerC, AnswerD, CorrectAnswer) VALUES
(@DiaID, N'Quốc gia nào có diện tích lớn nhất thế giới?', N'Trung Quốc', N'Hoa Kỳ', N'Nga', N'Canada', 'C'),
(@DiaID, N'Châu lục nào lạnh nhất thế giới?', N'Châu Á', N'Châu Âu', N'Nam Cực', N'Châu Phi', 'C'),
(@DiaID, N'Việt Nam có bờ biển dài bao nhiêu km?', N'3260 km', N'3450 km', N'3000 km', N'2850 km', 'A'),
(@DiaID, N'Sông Amazon nằm ở châu lục nào?', N'Châu Á', N'Châu Phi', N'Châu Nam Mỹ', N'Châu Âu', 'C');

-- Môn Lập trình
INSERT INTO Questions (SubjectID, Content, AnswerA, AnswerB, AnswerC, AnswerD, CorrectAnswer) VALUES
(@CodeID, N'Java được phát triển bởi công ty nào?', N'Microsoft', N'Apple', N'Sun Microsystems', N'Google', 'C'),
(@CodeID, N'Đâu là ngôn ngữ lập trình kịch bản thường dùng trên trình duyệt?', N'Python', N'Java', N'C++', N'JavaScript', 'D'),
(@CodeID, N'SQL viết tắt của từ gì?', N'Strong Question Language', N'Structured Query Language', N'Simple Query Language', N'Standard Query Language', 'B'),
(@CodeID, N'Kiểu dữ liệu "boolean" có mấy giá trị?', N'1', N'2', N'3', N'Vô số', 'B'),
(@CodeID, N'Mô hình OOP có mấy tính chất cơ bản?', N'2', N'3', N'4', N'5', 'C');
GO

-- 3. Chèn Bài thi
DECLARE @AdminID INT = (SELECT UserID FROM Users WHERE Username = 'admin');
DECLARE @ToanID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Toán');
DECLARE @AnhID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Tiếng Anh');
DECLARE @CodeID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Lập trình');

INSERT INTO Exams (TeacherID, Title, SubjectID, QuestionCount, Duration) VALUES
(@AdminID, N'Bài Kiểm tra Toán Học kì 1', @ToanID, 5, 20),
(@AdminID, N'Kiểm tra Tiếng Anh 15 phút', @AnhID, 5, 15),
(@AdminID, N'Đề thi thử Lập trình cơ bản', @CodeID, 5, 30);
GO

-- 4. Liên kết Bài thi - Câu hỏi
DECLARE @ExamToan INT = (SELECT ExamID FROM Exams WHERE Title = N'Bài Kiểm tra Toán Học kì 1');
DECLARE @ExamAnh INT = (SELECT ExamID FROM Exams WHERE Title = N'Kiểm tra Tiếng Anh 15 phút');
DECLARE @ExamCode INT = (SELECT ExamID FROM Exams WHERE Title = N'Đề thi thử Lập trình cơ bản');

-- Bài Toán (5 câu Toán)
INSERT INTO ExamQuestions (ExamID, QuestionID)
SELECT TOP 5 @ExamToan, QuestionID FROM Questions WHERE SubjectID = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Toán') ORDER BY QuestionID ASC;

-- Bài Tiếng Anh (5 câu Tiếng Anh)
INSERT INTO ExamQuestions (ExamID, QuestionID)
SELECT TOP 5 @ExamAnh, QuestionID FROM Questions WHERE SubjectID = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Tiếng Anh') ORDER BY QuestionID ASC;

-- Bài Lập trình (5 câu Code)
INSERT INTO ExamQuestions (ExamID, QuestionID)
SELECT TOP 5 @ExamCode, QuestionID FROM Questions WHERE SubjectID = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Lập trình') ORDER BY QuestionID ASC;
GO

-- 5. Tạo kết quả làm bài giả cho Học sinh
DECLARE @StudentID INT = (SELECT UserID FROM Users WHERE Username = 'student1');
DECLARE @ExamToan INT = (SELECT ExamID FROM Exams WHERE Title = N'Bài Kiểm tra Toán Học kì 1');
DECLARE @ToanID INT = (SELECT SubjectID FROM Subjects WHERE SubjectName = N'Toán');

IF @StudentID IS NOT NULL
BEGIN
    -- Một kết quả luyện tập môn Toán
    INSERT INTO QuizResults (StudentID, ExamID, SubjectID, ResultType, CorrectCount, TotalCount, DurationInSeconds, DateTaken)
    VALUES (@StudentID, NULL, @ToanID, 'PRACTICE', 4, 5, 120, GETDATE());

    DECLARE @PractResultID INT = SCOPE_IDENTITY();
    
    -- Chi tiết làm bài luyện tập
    INSERT INTO QuizAttemptDetails (ResultID, QuestionID, SelectedAnswer, IsCorrect)
    SELECT @PractResultID, QuestionID, CorrectAnswer, 1 
    FROM Questions WHERE SubjectID = @ToanID; -- Gỉa sử đúng hết

    -- Một kết quả bài thi Toán
    INSERT INTO QuizResults (StudentID, ExamID, SubjectID, ResultType, CorrectCount, TotalCount, DurationInSeconds, DateTaken)
    VALUES (@StudentID, @ExamToan, @ToanID, 'EXAM', 3, 5, 300, GETDATE());
    
    DECLARE @ExamResultID INT = SCOPE_IDENTITY();
    
    -- Thêm chi tiết làm bài thi (Câu 1-3 đúng, Câu 4-5 sai)
    DECLARE @Q1 INT = (SELECT QuestionID FROM Questions WHERE SubjectID = @ToanID ORDER BY QuestionID ASC OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY);
    DECLARE @Q2 INT = (SELECT QuestionID FROM Questions WHERE SubjectID = @ToanID ORDER BY QuestionID ASC OFFSET 1 ROWS FETCH NEXT 1 ROWS ONLY);
    DECLARE @Q3 INT = (SELECT QuestionID FROM Questions WHERE SubjectID = @ToanID ORDER BY QuestionID ASC OFFSET 2 ROWS FETCH NEXT 1 ROWS ONLY);
    DECLARE @Q4 INT = (SELECT QuestionID FROM Questions WHERE SubjectID = @ToanID ORDER BY QuestionID ASC OFFSET 3 ROWS FETCH NEXT 1 ROWS ONLY);
    DECLARE @Q5 INT = (SELECT QuestionID FROM Questions WHERE SubjectID = @ToanID ORDER BY QuestionID ASC OFFSET 4 ROWS FETCH NEXT 1 ROWS ONLY);

    INSERT INTO QuizAttemptDetails (ResultID, QuestionID, SelectedAnswer, IsCorrect) VALUES (@ExamResultID, @Q1, 'B', 1);
    INSERT INTO QuizAttemptDetails (ResultID, QuestionID, SelectedAnswer, IsCorrect) VALUES (@ExamResultID, @Q2, 'B', 1);
    INSERT INTO QuizAttemptDetails (ResultID, QuestionID, SelectedAnswer, IsCorrect) VALUES (@ExamResultID, @Q3, 'B', 1);
    -- Câu 4 đạo hàm x^2 là 2x (B), hs chọn C
    INSERT INTO QuizAttemptDetails (ResultID, QuestionID, SelectedAnswer, IsCorrect) VALUES (@ExamResultID, @Q4, 'C', 0);
    -- Câu 5 Tích phân 2x là x^2+C (D), hs chọn A
    INSERT INTO QuizAttemptDetails (ResultID, QuestionID, SelectedAnswer, IsCorrect) VALUES (@ExamResultID, @Q5, 'A', 0);
END
GO
