package swing;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class NewSimple {
    public static Connection makeDB() {

        Connection con = null;
        Statement stmt = null;
        String url = "jdbc:mysql://localhost:3306/micom";
        String id = "root";
        String password = "1324";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("드라이버 적재 성공");
            con = DriverManager.getConnection(url, id, password);
            System.out.println("데이터베이스 연결 성공");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버를 찾을 수 없습니다.");
        } catch (SQLException sqle) {
            System.out.println("연결에 실패하였습니다.");
        }
        return con;
    }

    private JFrame frame;
    private JTextField addArea;
    private JButton resetB;
    private JButton debugB;
    private JScrollPane scrollPane;
    private JTextArea textArea;
    private JTextArea resultArea;
    private JFrame debug;
    private JTextField tinstructionCounter;
    private JTextField tinstructionRegister;
    private JTextField toperationCode;
    private JTextField toperand;
    private JTextField taccumulators;

    private static final int MEMORYSIZE = 100;

    // 100개의 메모리 슬롯과 누산기 정의
    int instructionCounter = 0;
    int accumulator = 0;
    int memory[] = new int[MEMORYSIZE];
    int instructionRegister = 0;
    int operationCode = 0;
    int operand = 0;
    String log = "";
    int lnum = 0;
    int dbugNum = 1;
    int dbugLastNum=0;
    String viewM = "";
    boolean checkBranch = false;
    private static Connection conn = null;
    private static PreparedStatement pstmt = null;
    private static ResultSet rs = null;

    // 모든 옵코드 정의
    private static final int READ = 10;
    private static final int WRITE = 11;
    private static final int LOAD = 20;
    private static final int STORE = 21;
    private static final int ADD = 30;
    private static final int SUBSTRACT = 31;
    private static final int MULTIPLY = 33;
    private static final int DIVIDE = 32;
    private static final int BRANCH = 40;
    private static final int BRANCHNEG = 41;
    private static final int BRANCHZERO = 42;
    private static final int HALT = 43;

    /**
     * Create the application. //어플리케이션 만들기
     */
    public NewSimple() {
        initialize();
    }

    //메모리 화면 출력용
    //accumulator           (누산기)산술 및 논리 연산을 수행하는 데에 사용되며, 연산 결과를 저장
    //printRegistersAndMemory 실행 중인 프로그램의 레지스터와 메모리 상태를 출력
    //instructionCounter     명령어의 실행을 추적하거나 세는 데 사용되는 변수또는 레지스터
    //instructionRegister     실행 중인 명령어를 저장하는 레지스터
    //operationCode           연산을 식별
    //operand              연산이나 동작을 수행하기 위해 사용되는 값
    public void printRegistersAndMemory(int accumulator,int instructionCounter
            ,int instructionRegister, int operationCode, int operand, int[] memory) {
        //출력
        viewM = String.format("REGISTERS :\r\n");
        if(accumulator>=0)
            viewM += String.format("%-27s"+"+%04d\r\n","accumulator",accumulator);
        else
            viewM += String.format("%-27s"+"%05d\r\n","accumulator",accumulator);

        viewM += String.format("%-31s"+"%02d\r\n","instructionCounter",instructionCounter);
        viewM += String.format("%-27s"+"+%4d\r\n","instructionRegister",instructionRegister);
        viewM += String.format("%-28s"+"%02d\r\n","operationCode",operationCode);
        viewM += String.format("%-33s"+"%02d\r\n\r\n","operand",operand);

        viewM += String.format("MEMORY :\r\n");
        viewM += String.format("%5s"," ");
        for(int i =0; i<10; i++) {
            viewM += String.format("%-4s%7d"," ",i);
        }

        for(int i =0,j =0; i<MEMORYSIZE; i++,j++) {
            if(i%10==0)
                viewM += String.format("\r\n%-2s%02d"," ",j);

            if(memory[i]>=0)
                viewM += String.format("%-4s+%04d"," ",memory[i]);
            else
                viewM += String.format("%-4s%05d "," ",memory[i]);
        }
        viewM += String.format("\r\n\r\n");
        resultArea.setText(viewM);

        insertNum(accumulator,instructionCounter,instructionRegister,operationCode,operand,viewM);
        dbugLastNum++;
        //출력
    }

    //DB
    public String selectNum(int num){
        //DB connector
        String result = "";
        String sql = "select * from simple where num = ? ";
        conn = makeDB();

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,num);
            rs = pstmt.executeQuery();

            if(rs.next()){
                result = rs.getString("viewm");
            }

        }catch (Exception exception){
            textArea.append("\r\nDB오류");
        }finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean alterNum(){
        //DB connector
        boolean result = false;
        String sql = "alter table simple auto_increment = 1 ";
        //alter table [테이블명] auto_increment =[시작할 값 1] 키값 자동으로 증가
        conn = makeDB();

        try {
            pstmt = conn.prepareStatement(sql);

            result = pstmt.execute();

        }catch (Exception exception){
            textArea.append("\r\nDB오류"); //textArea.append 특정내용 추가
        }finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public int deleteAll(){
        //DB connector
        int result = 0;
        String sql = "delete from simple ";
        conn = makeDB();

        try {
            pstmt = conn.prepareStatement(sql);

            result = pstmt.executeUpdate();

        }catch (Exception exception){
            textArea.append("\r\nDB오류");
        }finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public int insertNum(int acc, int ic, int ir, int opc, int operand, String viewm){
        //DB connector
        int result = 0;
        String sql = "insert into simple values (?,?,?,?,?,?,null)"; //여러개의 데이터 한 테이블에 한번에 insert
        conn = makeDB();

        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1,acc);
            pstmt.setInt(2,ic);
            pstmt.setInt(3,ir);
            pstmt.setInt(4,opc);
            pstmt.setInt(5,operand);
            pstmt.setString(6,viewm);

            result = pstmt.executeUpdate();

        }catch (Exception exception){
            textArea.append("\r\nDB오류");
        }finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    //메모리 읽어버리는 메서드
    public void readMemory(){
        while(true) {
            instructionRegister = memory[instructionCounter]; //현재 실행문장
            if(instructionRegister<0)
                textArea.append("*** Data position Error ***");

            operationCode = instructionRegister/100;//앞 2개( 명령어)
            operand = instructionRegister%100;

            switch(operationCode) {//명령어 확인

                case READ :
                    textArea.append("\r\n*** insert value, -9999<= value <= 9999 ***\r\n");
                    memory[operand] = Integer.parseInt(JOptionPane.showInputDialog("정수를 입력하세요:"));
                    textArea.append(operand+"번 주소에 값 "+memory[operand]+"를 저장");

                    if((memory[operand]<-9999||memory[operand]>+9999)) {
                        textArea.append("\r\n*** because out of range, Simpletron execution terminated ***\r\n");
                        return;
                    }
                    break;

                case WRITE :
                    textArea.append("\r\n"+operand+"번 주소의 값 출력 : "+memory[operand]+"\r\n");
                    break;

                case LOAD :
                    accumulator = memory[operand];//데이터를
                    break;

                case STORE :
                    memory[operand] = accumulator; //누산기값을
                    break;

                case ADD :
                    accumulator += memory[operand];//데이터를
                    break;

                case SUBSTRACT :
                    accumulator -= memory[operand];//데이터를
                    break;

                case DIVIDE :
                    if(memory[operand]==0) {//데이터가
                        textArea.append("\r\n*** Attempt to divide by zero ***\r\n");
                        return;
                    }
                    accumulator /= memory[operand];//데이터를
                    break;

                case MULTIPLY :
                    accumulator *= memory[operand];//데이터를
                    break;

                case BRANCH :
                    instructionCounter = operand;//주소값으로 이동
                    checkBranch = true;
                    break;

                case BRANCHNEG :
                    if(accumulator<0) {
                        instructionCounter = operand;//주소값으로 이동
                        checkBranch = true;
                        break;
                    }
                    break;

                case BRANCHZERO :
                    if(accumulator == 0) {
                        instructionCounter = operand;//다음 실행문장 주소값으로 이동
                        checkBranch = true;
                        break;
                    }
                    break;

                case HALT :
                    printRegistersAndMemory(accumulator,instructionCounter,instructionRegister
                            , operationCode, operand, memory);
                    textArea.append("\r\n*** Simpletron execution terminated ***\r\n");
                    return;

                default :
                    textArea.append("\r\n*** Doesn't exist operationCode ***\r\n");
                    return;
            }

            printRegistersAndMemory(accumulator,instructionCounter,instructionRegister
                    , operationCode, operand, memory);

            if(checkBranch==false){
                checkBranch=false;
                instructionCounter++;//다음 메모리 위치
            }

        }
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {

        frame = new JFrame();
        frame.getContentPane().setBackground(new Color(138, 43, 226));
        frame.setBounds(100, 100, 800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        scrollPane = new JScrollPane();
        scrollPane.setBounds(12, 10, 760, 288);
        frame.getContentPane().add(scrollPane);

        textArea = new JTextArea();
        textArea.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        textArea.setEditable(false);
        deleteAll();
        alterNum();
        textArea.setText("*** 심플트론에 오신 것을 환영합니다 ***\n" + "*** 하나의 명령을 한 번에 입력 버튼을 클릭하여 입력하십시오. ***\n"
                + "*** -99999를 입력하면 프로그램이 시작됩니다. ***\n\n");
        scrollPane.setViewportView(textArea);

        addArea = new JTextField();
        addArea.setBounds(12, 308, 279, 29);
        addArea.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        addArea.setForeground(new Color(0, 0, 0));
        frame.getContentPane().add(addArea);
        addArea.setColumns(10);

        JButton LeftButton = new JButton("<-");

        LeftButton.setForeground(Color.BLACK);
        LeftButton.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        LeftButton.setBounds(12, 720, 97, 29);
        LeftButton.setVisible(false);
        frame.getContentPane().add(LeftButton);

        JButton RightButton = new JButton("->");
        RightButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbugNum++;
                if(dbugNum==dbugLastNum) {
                    RightButton.setVisible(false);
                }
                resultArea.setText(selectNum(dbugNum));
                LeftButton.setVisible(true);
            }
        });
        LeftButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbugNum--;
                if(dbugNum==1) {
                    LeftButton.setVisible(false);
                }
                resultArea.setText(selectNum(dbugNum));
                RightButton.setVisible(true);
            }
        });
        RightButton.setForeground(Color.BLACK);
        RightButton.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        RightButton.setBounds(121, 720, 97, 29);
        RightButton.setVisible(false);
        frame.getContentPane().add(RightButton);

        JButton dbugB = new JButton("디버그");
        // 디버그 버튼을 비활성화할 때 클릭 이벤트 리스너를 제거하는 코드
        dbugB.setEnabled(false);
        for (ActionListener al : dbugB.getActionListeners()) {
            dbugB.removeActionListener(al);
        }
        dbugB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 디버그 버튼 클릭 시 실행할 동작
                LeftButton.setVisible(false);
                RightButton.setVisible(true);
                resultArea.setText(selectNum(dbugNum));
            }
        });
        dbugB.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        dbugB.setBounds(636, 720, 136, 29);
        frame.getContentPane().add(dbugB);

        JButton addB = new JButton("입력");
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log = addArea.getText();
                try {
                    lnum = Integer.parseInt(log);
                    if (lnum == -99999) {
                        textArea.append("데이터 저장");
                        //초기화할것들
                        operand = 0;
                        log = "";
                        lnum = 0;
                        addArea.setText("");
                        addB.setEnabled(false);
                        textArea.append("\r\n***프로그램 로딩 완료***\r\n***시작하겠습니다.***\r\n");

                        readMemory();

                        //입력 버튼 비활 코드 (완료했으니 입력 비활 초기화 누르면 활성화되게)??
                        // 이 부분에서 입력 버튼을 비활성화하는 코드를 넣을 수 있습니다.

                        //리셋버튼으로 가서 클릭하면 입력 활성화?? 리셋 메서드 만들기
                        // 리셋 버튼을 눌렀을 때 입력 버튼을 다시 활성화하는 코드를 넣을 수 있습니다.
                        // 입력 버튼 비활성화
                        addB.setEnabled(false);
                        dbugB.setEnabled(true);
                    } else if (lnum < -9999 || lnum > +9999) {
                        //입력 버튼 비활??
                        // 범위를 벗어난 경우의 처리 내용입니다.

                        throw new OutOfMemoryError();
                    } else {
                        if (lnum >= 0) {
                            log = String.format("%02d ? +%04d\r\n", operand, lnum);
                        } else {
                            log = String.format("%02d ? %05d\r\n", operand, lnum);
                        }

                        textArea.append(log);
                        memory[operand] = lnum;
                        operand++;
                        //값 다 넣었으면 항상 초기화??더있는지 확인
                        log = "";
                        lnum = 0;
                        addArea.setText("");
                    }
                } catch (OutOfMemoryError om) {
                    textArea.append("\r\n***범위를 벗어났기 때문에 메모리를 초기화 하겠습니다.***\r\n");
                } catch (Exception ex) {
                    textArea.append("\r\n***에러발생 초기화후 다시 시작해주세요***\r\n");
                    //초기화만 활성화 나머지 비활??
                    // 에러 발생 시의 처리 내용입니다.
                }
            }
        };

        addB.addActionListener(actionListener);

        // 키 이벤트 처리 (엔터 키)
        addArea.addKeyListener((KeyListener) new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
                }
            }
        });


        addB.setBounds(303, 308, 97, 29);
        addB.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        addB.setForeground(new Color(0, 0, 0));
        frame.getContentPane().add(addB);

        resultArea = new JTextArea();
        resultArea.setBounds(12, 347, 760, 365);
        resultArea.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        resultArea.setEditable(false);
        frame.getContentPane().add(resultArea);



        resetB = new JButton("초기화");
        resetB.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {//resetB클릭
                // 주요 변수들 초기화
                accumulator = 0;
                instructionCounter = 0;
                instructionRegister = 0;
                operationCode = 0;
                operand = 0;
                log = "";
                lnum = 0;
                viewM = "";
                checkBranch = false;//??
                addB.setEnabled(true);
                dbugB.setEnabled(false);
                LeftButton.setVisible(false);
                RightButton.setVisible(false);

                deleteAll();
                alterNum();

                // 메모리 초기화
                for (int i = 0; i < MEMORYSIZE; i++) {
                    memory[i] = 0;
                }

                // 화면 초기화
                textArea.setText("*** 심플트론에 오신 것을 환영합니다 ***\n" +
                        "*** 하나의 명령을 한 번에 입력 버튼을 클릭하여 입력하십시오. ***\n" +
                        "*** -99999를 입력하면 프로그램이 시작됩니다. ***\n\n");
                resultArea.setText(""); // 결과 텍스트 영역 초기화
            }
        });

        resetB.setBounds(412, 308, 97, 29);
        resetB.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        resetB.setForeground(new Color(0, 0, 0));
        frame.getContentPane().add(resetB);
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    NewSimple window = new NewSimple();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}