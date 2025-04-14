/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

public class MembershipModel {
    private String name;
    private String studentId;
    private String password;

    // ✅ 기본 생성자 추가
    public MembershipModel() {
        this.name = "";
        this.studentId = "";
        this.password = "";
    }

    // 기존 생성자
    public MembershipModel(String name, String studentId, String password) {
        this.name = name;
        this.studentId = studentId;
        this.password = password;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // 회원가입 처리 (단순히 로그로 처리)
    public boolean register() {
        try {
            java.io.FileWriter writer = new java.io.FileWriter("users.txt", true); // append 모드
            writer.write(name + "," + studentId + "," + password + "\n");
            writer.close();
            System.out.println("회원가입 성공: " + name + " (" + studentId + ")");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
