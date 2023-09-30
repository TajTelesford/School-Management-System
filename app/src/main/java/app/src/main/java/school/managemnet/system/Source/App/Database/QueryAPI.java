package app.src.main.java.school.managemnet.system.Source.App.Database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import app.src.main.java.school.managemnet.system.Source.App.CourseComponenets.Assignment;
import app.src.main.java.school.managemnet.system.Source.App.CourseComponenets.course;
import app.src.main.java.school.managemnet.system.Source.App.HeadlessConfig.DataConfigTypes.AssignmentType;
import app.src.main.java.school.managemnet.system.Source.App.MessageProtocol.MessageType;
import app.src.main.java.school.managemnet.system.Source.App.UserFunctionalty.User;
import app.src.main.java.school.managemnet.system.Source.App.UserFunctionalty.Faculty.FacultyImpl;
import app.src.main.java.school.managemnet.system.Source.App.UserFunctionalty.Student.StudentImpl;

public class QueryAPI {
    //TODO: Finish Query Class

    static Connection connection = null;
    DATABASECONNECTION DB = null;
    public QueryAPI(DATABASECONNECTION database)
    {
        DB = database;
        OpenConnection();
        InitConnection();
    }

    public void QueryShutdown()
    {
        CloseConnection();
    }

    //SIGN UP FUNCTIONALITY
    public int[] AppStart_FetchUserIDS() throws SQLException
    {
        String query = "SELECT user_id from users";
        PreparedStatement pStatement = connection.prepareStatement(query);
        ResultSet resultSet = pStatement.executeQuery();

        int size_of_set = resultSet.getFetchSize();
        int[] List_Of_UserIDS = new int[ size_of_set + (User.MAX_NUMBER_OF_IDS - size_of_set) ];

        int i = 0;
        while( resultSet.next() )
        {
            int id = resultSet.getInt("user_id");
            List_Of_UserIDS[i] = id;
            i++;
        }
        return List_Of_UserIDS;
    }

    //LOGIN FUNCTIONALITY
    public ResultSet Login_LogIntoDatabase(String HashedPassword, int ID) throws SQLException
    {
        String login_query = "SELECT * FROM users WHERE user_id = ? AND password = ?";
        PreparedStatement pStatement = connection.prepareStatement(login_query);
        pStatement.setInt(1, ID);
        pStatement.setString(2, HashedPassword);
        ResultSet UserData = pStatement.executeQuery();
        
        return UserData;
    }

    public void OpenConnection() { DB.SecureConnection(); }

    public void CloseConnection() { DB.CloseConnection(); }
    
    //Has to be used after a open connection function is made
    private void InitConnection()
    {
        connection = DB.GetConnection();
    }    
    
    //ADMIN FUNCTIONALITY
    public void Admin_ShowUsers() throws SQLException
    {
        String query = "SELECT * From users";
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
        } catch (SQLException e) {
            
            e.printStackTrace();
        }
        finally
        {
            while( resultSet.next() )
            {
                System.out.println("-------------------------------------");
                System.out.println("ID: { " + resultSet.getInt("user_id") + " }");
                System.out.println("TYPE: { " + resultSet.getString("user_type") + " }");
                System.out.println("NAME: { " + resultSet.getString("name") + " }");
                System.out.println("EMAIL: { " + resultSet.getString("email") + " }");
                System.out.println("-------------------------------------");
            }
        }
        
    }

    public void Admin_AddUser(User user) throws SQLException
    {
        OpenConnection();
        InitConnection();
        PreparedStatement pStatement = null;
        String insertQuery = "INSERT INTO users (user_id, user_type, name, email, password) VALUES (?, ?, ?, ?, ?)";

        try {
            pStatement = connection.prepareStatement(insertQuery);
        
            pStatement.setInt(1, user.getUserID());
            pStatement.setString(2, user.getUserType());
            pStatement.setString(3, user.getUserName());
            pStatement.setString(4, user.getUserEmail());
            pStatement.setString(5, user.getUserPassword());
        
            int rows = pStatement.executeUpdate();
        
            System.out.println(rows + " row(s) inserted into system");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the PreparedStatement here if needed
        }
        
    }

    public void Admin_DeleteUser(User user) throws SQLException
    {
        String deleteQuery = "DELETE FROM users WHERE user_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
        preparedStatement.setInt(1, user.getUserID());

        int rowsDeleted = preparedStatement.executeUpdate();
        System.out.println(rowsDeleted + " row(s) deleted");
    }

    public void Admin_MakeCourses(Scanner sc) throws SQLException
    {
        /*
         * Find All Teachers
         * Select A Teacher
         * Give Them A Course(s)
         */

        List<FacultyImpl> ListOfTeachers = new ArrayList<>();
        //Teachers = 1
        //Students = 2
        String query = "SELECT * FROM users WHERE user_type = 1";
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
        } catch (SQLException e) {
            
            e.printStackTrace();
        }
        finally
        {
            while( resultSet.next() )
            {
                int id = resultSet.getInt("user_id");
                String email = resultSet.getString("email");
                String name = resultSet.getString("name");

                //We Don't Need The Passwords To Be Used {(Password) -> null}
                ListOfTeachers.add(new FacultyImpl(name, email, null, id)); 
            }
        }
        resultSet.close();
        //Select A Teacher
        for(int i = 0; i < ListOfTeachers.size(); i++)
        {
            System.out.println(i+1 + ": " + ListOfTeachers.get(i).getUserName());
        }
        System.out.print("Select Teacher: ");

        //TODO: Create Validation For User Selection
        int choice = sc.nextInt() - 1;

        //Give Teacher A Course(s)
        System.out.print("Name Of Course: ");
        sc.nextLine();
        String course_name = sc.nextLine();
        Courses_ConnectFacultyToCourse(ListOfTeachers.get(choice), course_name);
        System.out.println();

    }
    
    public void Admin_ConnectStudentToCourse(Scanner sc) throws SQLException
    {
        //search through the list of students
        //pick a student
        //get a list of courses
        //pick a course
        List<StudentImpl> list_of_students = new ArrayList<>();
        List<course> list_of_courses = new ArrayList<>();

        list_of_students = Helper_ReturnListOfStudents();
        Helper_PrintStudents(list_of_students);

        //Create Validation Here
        System.out.println("Which Student?");
        int choice = sc.nextInt() - 1;
        StudentImpl student = list_of_students.get(choice);

        list_of_courses = Helper_ReturnListOfCourses();
        Helper_PrintCourses(list_of_courses);

        System.out.println("Which Class?");
        choice = sc.nextInt() - 1;

        course Course = list_of_courses.get(choice);

        Courses_AddStudentToCourse(student.getUserID(), Course.GetCourseID());
    }

    //Messaging Functionality
    static public void QueryMessageApi(int s_id, int r_id, String sender, String subject, String msg)
    {
        String query = "insert into messages (sender_id, recipient_id, sender, subject, message) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pStatement = null;
        try {
            pStatement = connection.prepareStatement(query);
        
            pStatement.setInt(1, s_id);
            pStatement.setInt(2, r_id);
            pStatement.setString(3, sender);
            pStatement.setString(4, subject);
            pStatement.setString(5, msg);
        
            int rows = pStatement.executeUpdate();
        
            System.out.println(rows + " row(s) inserted into system");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the PreparedStatement here if needed
        }
        
    }

    public static List<MessageType> GetMessages(int id) 
    {
        List<MessageType> messages = new ArrayList<>();

        String query = "SELECT * " +
                        "FROM messages " +
                        "WHERE recipient_id = ?";
        
        ResultSet resultSet = null;

        try (PreparedStatement pStatement = connection.prepareStatement(query)){
            pStatement.setInt(1, id);
            resultSet = pStatement.executeQuery();

            while( resultSet.next() )
                {
                    int sender_id = resultSet.getInt("sender_id");
                    int recipient_id = resultSet.getInt("recipient_id");
                    String sender = resultSet.getString("sender");
                    String subject = resultSet.getString("subject");
                    String msg = resultSet.getString("message");

                    messages.add(
                        new MessageType(sender_id, 
                                        recipient_id, 
                                        sender, 
                                        subject, 
                                        msg
                            )
                    );
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    //FACULTY FUNCTIONALITY
    public void Faculty_ShowCourses(FacultyImpl user) throws SQLException
    {
        String query = "SELECT * FROM courses WHERE teacher_id = ?";
        PreparedStatement pStatement = connection.prepareStatement(query);

        pStatement.setInt(1, user.getUserID());

        ResultSet resultSet = pStatement.executeQuery();
        System.out.println("====================================");
        while( resultSet.next() )
        {
            int course_id = resultSet.getInt("course_id");
            String course_name = resultSet.getString("course_name");
            System.out.println("Course: " + course_name + " Section #: " + course_id);
        }
        System.out.println("====================================");
        
    }

    public void Faculty_GetGrades(){}

    public void Faculty_SetGrades(){}

    public void Faculty_ChangeGrades(){}

    public void Faculty_PostAssignment(Assignment assignment, FacultyImpl user) throws SQLException 
    {
        String assignment_query = "INSERT INTO assignments (assignment_id, course_id, assignment_name,"+
                                    "due_date, correct_answers, assignment_description," +
                                    "assignment_image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pStatement = connection.prepareStatement(assignment_query);

        pStatement.setInt(1, assignment.GetAssignmentID());
        pStatement.setInt(2, Helper_GetCourseIDFromTeacher(user));
        pStatement.setString(3, assignment.GetAssignmentName());
        pStatement.setString(4, assignment.GetAssignmentDueDate());
        pStatement.setString(5, assignment.GetAssignmentCorrectAnswers());
        pStatement.setString(6, assignment.GetAssignmentDescription());
        pStatement.setBytes(7, assignment.GetAssignmentImage());

        pStatement.executeUpdate();
    }

    public void Faculty_DeleteAssignmet(Assignment assignment, User user) throws SQLException {}

    public void Faculty_GradeAssignment(Assignment assignment, User user) throws SQLException {}

    //Student Functionality
    public void Student_SubmitAssignment(StudentImpl student, String answers, AssignmentType assignment) throws SQLException
    {
        String query = "INSERT INTO submissions (student_id, assignment_id, student_answers, submission_time) " +
                       "VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            // Set the parameters for the SQL query
            preparedStatement.setInt(1, student.getUserID());
            preparedStatement.setInt(2, assignment.GetAssignmentTypeID());
            preparedStatement.setString(3, answers);

            // Get the current timestamp as the submission time
            Timestamp submissionTime = new Timestamp(new Date(System.currentTimeMillis()).getTime());
            preparedStatement.setTimestamp(4, submissionTime);

            // Execute the SQL query to insert the submission
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("ROWS AFFECTED: " + rowsAffected);
        }
    }
    //Course Functionality
    public void Courses_ConnectFacultyToCourse(FacultyImpl teacher, String name) throws SQLException
    {
        String query = "INSERT INTO courses (course_id, teacher_id, course_name) VALUES (?, ?, ?);";
        PreparedStatement pStatement = connection.prepareStatement(query);

        //Create a better course Id generator
        pStatement.setInt(1, new Random().nextInt(10000000));
        pStatement.setInt(2, teacher.getUserID());
        pStatement.setString(3, name);

        int rowsAffected = pStatement.executeUpdate();
        System.out.println("ROWS AFFECTED: " + rowsAffected);
    }

    private void Courses_AddStudentToCourse(int student_id, int course_id) throws SQLException
    {
        String insertQuery = "INSERT INTO Student_Courses (student_id, course_id) VALUES (?, ?)";

        try (PreparedStatement pStatement = connection.prepareStatement(insertQuery)) {
            pStatement.setInt(1, student_id);
            pStatement.setInt(2, course_id);

            int rowsInserted = pStatement.executeUpdate();

            System.out.println("ROWS AFFECTED: " + rowsInserted);
            
        }
    }

    //General Functions
    public AssignmentType OpenAssignment(FacultyImpl teacher, StudentImpl student, Scanner sc) throws SQLException 
    {
        AssignmentType assignment = null;
        if(student == null)
            assignment = Helper_TeacherOpenAssignment(teacher, sc);
        if(teacher == null)
            assignment = Helper_StudentOpenAssignment(student, sc);

        return assignment;
    }

    //Helper Functions
    public int Helper_GetCourseIDFromTeacher(FacultyImpl teacher) throws SQLException
    {
        int ID = -1;
        String query = "SELECT course_id FROM courses WHERE teacher_id = ?";
        PreparedStatement pStatement = connection.prepareStatement(query);
        pStatement.setInt(1, teacher.getUserID());

        ResultSet resultSet = pStatement.executeQuery();
        while( resultSet.next() )
        {
            ID = resultSet.getInt("course_id");
        }
        return ID;
    }

    private Assignment Helper_GetAssignmentFromCourse(int course_id, int teacher_id, Scanner sc) throws SQLException
    {
        String query = "Select * from assignments where course_id = ?";
        List<Assignment> assignments = new ArrayList<>();

        PreparedStatement pStatement = connection.prepareStatement(query);
        pStatement.setInt(1, course_id);

        ResultSet resultSet = pStatement.executeQuery();

        int i = 0, choice;
        
        System.out.println("========================================");
        while( resultSet.next() )
        {
            String name = resultSet.getString("assignment_name");
            System.out.println(i + 1 + ": " + name);
            Assignment assignment = new Assignment(
                resultSet.getBytes("assignment_image"), 
                resultSet.getInt("assignment_id"),
                resultSet.getInt("course_id"),
                name,
                resultSet.getString("assignment_description"),
                resultSet.getString("due_date"),
                resultSet.getString("correct_answers")
            );
            assignments.add(assignment);
        }
        resultSet.close();
        System.out.println("========================================");
        System.out.print("Which Assignment?: ");
        //ADD VALIDATION HERE
        choice = sc.nextInt();

        return assignments.get(choice-1);
    }

    private AssignmentType Helper_StudentOpenAssignment(StudentImpl student, Scanner sc) throws SQLException
    {
        //Get All The Courses The Student Has
        //Student Picks A Course
        //Get All Assignments That The Teacher Has Posted
        //Student Picks An Assignment
        //Returns The Bytes Of The Assignment

        List<course> courses = new ArrayList<>();
        List<Assignment> assignments = new ArrayList<>();

        courses = Helper_GetStudentCourses(student.getUserID());
        Helper_PrintCourses(courses);

        System.out.println("Which Course?: ");

        //Add Validation Here
        int choice = sc.nextInt() - 1;

        assignments = Helper_GetStudentsAssignments(courses.get(choice).GetCourseID());
        Helper_PrintAssignments(assignments);

        //Add Validation Here
        choice = sc.nextInt() - 1;
        sc.nextLine();

        AssignmentType a = new AssignmentType(
                            assignments.get(choice).GetAssignmentImage(), 
                            assignments.get(choice).GetAssignmentID()
                        );
        return a;
    }

    private void Helper_PrintAssignments(List<Assignment> assignments) 
    {
        for(int i = 0; i < assignments.size(); i++)
            System.out.println(i + 1 + 
                            ": " + assignments.get(i).GetAssignmentName());
                
        
    }

    private List<Assignment> Helper_GetStudentsAssignments(int course_id) 
    {
        List<Assignment> assignments = new ArrayList<>();
        String query = "SELECT * " +
                       "FROM assignments " +
                       "WHERE course_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, course_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) 
            {
                assignments.add(
                    new Assignment(
                        resultSet.getBytes("assignment_image"), 
                        resultSet.getInt("assignment_id"),
                        resultSet.getInt("course_id"),
                        resultSet.getString("assignment_name"),
                        resultSet.getString("assignment_description"),
                        resultSet.getString("due_date"),
                        resultSet.getString("correct_answers")
                    )
                );
            }
            } 
            catch (SQLException e) 
            {
                e.printStackTrace();
            }
        return assignments;
    }

    private List<course> Helper_GetStudentCourses(int student_id) throws SQLException
    {
        List<course> courses = new ArrayList<>();
        String query = "SELECT courses.course_name, courses.course_id FROM courses " +
                        "INNER JOIN student_courses ON courses.course_id = Student_courses.course_id" + 
                        " WHERE student_courses.student_id = ?";
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;
        try {
            pStatement = connection.prepareStatement(query);
            pStatement.setInt(1, student_id);
            resultSet = pStatement.executeQuery();
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally
        {
            while( resultSet.next() )
            {
                courses.add(
                    new course(resultSet.getString("course_name"), 
                    resultSet.getInt("course_id"))
                );
            }
        }
        return courses;
    }

    private AssignmentType Helper_TeacherOpenAssignment(FacultyImpl teacher, Scanner sc) throws SQLException
    {
        /*
         * Get a list of all assignments a teacher has
         * User picks a assignment to open
         * Fetches the bytes from the LONGBLOB Image 
         * Returns the bytes to the caller
         */
        String teacher_query = "SELECT * FROM courses WHERE teacher_id = ?";
        PreparedStatement pStatement = connection.prepareStatement(teacher_query);

        pStatement.setInt(1, teacher.getUserID());

        ResultSet resultSet = pStatement.executeQuery();
        List<course> courseId_list = new ArrayList<>();
        int choice, i = 0;
        System.out.println("Which Course Is The Assignment In?: ");
        while( resultSet.next() )
        {
            int course_id = resultSet.getInt("course_id");
            String course_name = resultSet.getString("course_name");
            System.out.println("Course "+ i+1 + ": " + course_name + " Section #: " + course_id);
            courseId_list.add(new course(course_name, course_id));
            i++;
        }
        //Add Validation HERE
        choice = sc.nextInt();
        course selected_course = courseId_list.get(choice - 1);
        Assignment assignment = Helper_GetAssignmentFromCourse(selected_course.GetCourseID(), teacher.getUserID(), sc);

        AssignmentType a = new AssignmentType(
                            assignment.GetAssignmentImage(), 
                            assignment.GetAssignmentID()
                            );

        return a; //returns image bytes
    }

    private void Helper_PrintCourses(List<course> courses) 
    {
        for(int i = 0; i < courses.size(); i++)
        {
            System.out.println(
                "(" + (i + 1) + ")\n" +
                "Course Name: " +
                courses.get(i).GetCourseName() + " " +
                "Course ID: " +
                courses.get(i).GetCourseID()
        );
        }
    }   

    private void Helper_PrintStudents(List<StudentImpl> students) 
    {
        for(int i = 0; i < students.size(); i++)
            System.out.println(
                                "(" + (i + 1) + ")" +
                                "NAME: " +
                                students.get(i).getUserName() + 
                                " ID: " +
                                students.get(i).getUserID());
    }

    private List<StudentImpl> Helper_ReturnListOfStudents() throws SQLException
    {
        List<StudentImpl> student_list = new ArrayList<>();
        //This get all students
        //Students Enum = 2
        String query = "SELECT * FROM users WHERE user_type = 2";

        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
        } catch (SQLException e) {
            
            e.printStackTrace();
        }
        finally
        {
            while( resultSet.next() )
            {
                int id = resultSet.getInt("user_id");
                String email = resultSet.getString("email");
                String name = resultSet.getString("name");

                //password is null because we don't need that information at this stage
                student_list.add(new StudentImpl(name, email, null, id));
            }
        }

        return student_list;
    }

    private List<course> Helper_ReturnListOfCourses() throws SQLException
    {
        List<course> course_ids = new ArrayList<>();
        String query = "select * from courses";
        Statement statement = null;
        ResultSet resultSet = null;
        try 
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        finally{
            while( resultSet.next() )
            {
                course_ids.add(new course(
                    resultSet.getString("course_name"), 
                    resultSet.getInt("course_id")));
            }
        }
        return course_ids;
    }

    public String Helper_GetPasswordFromEmail(String email) 
    {
        String password = null;
        String query = "Select password Where email = ?";
        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            pStatement.setString(1, email);

            ResultSet resultSet = pStatement.executeQuery();
            if( resultSet.next() )
                password = resultSet.getString("password");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(password == null) System.out.println("PASSWORD: NULL");
        return password;
    }

    public List<FacultyImpl> Helper_GetListOfTeachers(User user) 
    {
        String query = "SELECT u.* " +
                        "FROM users u " +
                        "INNER JOIN courses c ON u.user_id = c.teacher_id " +
                        "INNER JOIN student_courses sc ON c.course_id = sc.course_id " +
                        "WHERE sc.student_id = ?";
        List<FacultyImpl> teachers_list = new ArrayList<>();

        try(PreparedStatement pStatement = connection.prepareStatement(query))
        {
            pStatement.setInt(1, user.getUserID());
            ResultSet resultSet = pStatement.executeQuery();

            while( resultSet.next() )
            {
                teachers_list.add(
                    new FacultyImpl(
                        resultSet.getString("name"), 
                        resultSet.getString("email"), 
                        resultSet.getString("password"),
                        resultSet.getInt("user_id")
                    )
                );
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return teachers_list;
    }

    public List<StudentImpl> Helper_GetListOfStudents(User user)
    {
        List<StudentImpl> students = new ArrayList<>();
        String query = "SELECT students.* " +
                        "FROM users AS students " +
                        "JOIN Student_Courses ON students.user_id = Student_Courses.student_id " +
                        "JOIN courses ON Student_Courses.course_id = courses.course_id " +
                        "WHERE courses.teacher_id = ? AND students.user_type = 'student'";

        ResultSet resultSet = null;
        try(PreparedStatement pStatement = connection.prepareStatement(query)){
            pStatement.setInt(1, user.getUserID());
            resultSet = pStatement.executeQuery();

            while( resultSet.next() )
                    {
                        int id = resultSet.getInt("user_id");
                        String email = resultSet.getString("email");
                        String name = resultSet.getString("name");
                    
                        students.add( 
                            new StudentImpl(name, email, null, id)
                        );

                    }
                    resultSet.close();
            } catch (SQLException e) {      
                e.printStackTrace();
            }
        return students;
    }

}