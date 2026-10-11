import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class AttendanceManagementSystem extends JFrame {

    enum Status { PRESENT, ABSENT }

    // ---------- DATA CLASSES ----------

    static class Student {
        private final String id;
        private String name;
        private String className;

        Student(String id, String name, String className) {
            this.id = required(id, "Student ID");
            this.name = required(name, "Student name");
            this.className = required(className, "Class");
        }

        String getId() { return id; }
        String getName() { return name; }
        String getClassName() { return className; }

        void update(String name, String className) {
            this.name = required(name, "Student name");
            this.className = required(className, "Class");
        }
    }

    static class Subject {
        private final String code;
        private String name;

        Subject(String code, String name) {
            this.code = required(code, "Subject code").toUpperCase();
            this.name = required(name, "Subject name");
        }

        String getCode() { return code; }
        String getName() { return name; }
        void setName(String name) { this.name = required(name, "Subject name"); }
    }

    static class Faculty {
        private final String id;
        private String name;

        Faculty(String id, String name) {
            this.id = required(id, "Faculty ID").toUpperCase();
            this.name = required(name, "Faculty name");
        }

        String getId() { return id; }
        String getName() { return name; }
        void setName(String name) { this.name = required(name, "Faculty name"); }
    }

    static class Attendance {
        final String studentId;
        final String subjectCode;
        final String facultyId;
        final LocalDate date;
        final Status status;

        Attendance(String studentId, String subjectCode, String facultyId,
                   LocalDate date, Status status) {
            this.studentId = studentId;
            this.subjectCode = subjectCode;
            this.facultyId = facultyId;
            this.date = date;
            this.status = status;
        }
    }

    // ---------- REQUIRED COLLECTIONS ----------

    private final ArrayList<Student> students = new ArrayList<>();
    private final ArrayList<Subject> subjects = new ArrayList<>();
    private final ArrayList<Faculty> faculty = new ArrayList<>();
    private final LinkedList<Attendance> history = new LinkedList<>();
    private final HashMap<String, Student> studentById = new HashMap<>();
    private final Status[] statusArray = Status.values();

    private final JTabbedPane tabs = new JTabbedPane();
    private final File dataFile = new File("attendance-data.txt");

    public AttendanceManagementSystem() {
        super("College Attendance Management System");
        loadData();
        setJMenuBar(createMenu());
        add(tabs);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        refresh();
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");

        saveItem.addActionListener(e -> {
            try {
                saveData();
                message("Data saved.");
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });

        exitItem.addActionListener(e -> {
            try { saveData(); } catch (Exception ignored) { }
            System.exit(0);
        });

        fileMenu.add(saveItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    private void refresh() {
        int selectedTab = Math.max(0, tabs.getSelectedIndex());
        tabs.removeAll();
        tabs.addTab("Students", studentPanel());
        tabs.addTab("Subjects", subjectPanel());
        tabs.addTab("Faculty", facultyPanel());
        tabs.addTab("Attendance", attendancePanel());
        tabs.addTab("Reports", reportPanel());
        tabs.setSelectedIndex(Math.min(selectedTab, tabs.getTabCount() - 1));
    }

    // ---------- STUDENT MANAGEMENT ----------

    private JPanel studentPanel() {
        DefaultTableModel model = tableModel("ID", "Name", "Class", "Attendance %");
        fillStudentTable(model, students);
        JTable table = new JTable(model);
        JTextField searchField = new JTextField(15);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim().toLowerCase();
            ArrayList<Student> matches = new ArrayList<>();

            for (Student student : students) {
                if (student.getId().toLowerCase().contains(query)
                        || student.getName().toLowerCase().contains(query)) {
                    matches.add(student);
                }
            }

            model.setRowCount(0);
            fillStudentTable(model, matches);
        });

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> studentDialog(null));

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                message("Select a student.");
            } else {
                studentDialog(model.getValueAt(row, 0).toString());
            }
        });

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                message("Select a student.");
                return;
            }

            try {
                String id = model.getValueAt(row, 0).toString();
                students.remove(findStudent(id));
                studentById.remove(id);
                history.removeIf(record -> record.studentId.equals(id));
                saveData();
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });

        return panelWithTable(table, new JComponent[]{
                new JLabel("ID or name"), searchField, searchButton,
                addButton, updateButton, deleteButton
        });
    }

    private void studentDialog(String existingId) {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField classField = new JTextField("BCA III");

        if (existingId != null) {
            Student student = findStudent(existingId);
            idField.setText(student.getId());
            idField.setEditable(false);
            nameField.setText(student.getName());
            classField.setText(student.getClassName());
        }

        String title = existingId == null ? "Add Student" : "Update Student";
        JPanel form = form(
                new String[]{"Student ID", "Name", "Class"},
                new JComponent[]{idField, nameField, classField}
        );

        int choice = JOptionPane.showConfirmDialog(
                this, form, title, JOptionPane.OK_CANCEL_OPTION
        );
        if (choice != JOptionPane.OK_OPTION) return;

        try {
            if (existingId == null) {
                String id = required(idField.getText(), "Student ID");
                if (studentById.containsKey(id)) {
                    throw new IllegalArgumentException("Student ID already exists.");
                }
                Student student = new Student(id, nameField.getText(), classField.getText());
                students.add(student);
                studentById.put(id, student);
            } else {
                findStudent(existingId).update(nameField.getText(), classField.getText());
            }
            saveData();
            refresh();
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    // ---------- SUBJECT MANAGEMENT ----------

    private JPanel subjectPanel() {
        DefaultTableModel model = tableModel("Code", "Subject Name");
        for (Subject subject : subjects) {
            model.addRow(new Object[]{subject.getCode(), subject.getName()});
        }

        JTable table = new JTable(model);
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");

        addButton.addActionListener(e -> subjectDialog(null));
        updateButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) message("Select a subject.");
            else subjectDialog(model.getValueAt(row, 0).toString());
        });
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                message("Select a subject.");
                return;
            }
            try {
                String code = model.getValueAt(row, 0).toString();
                subjects.removeIf(subject -> subject.getCode().equals(code));
                history.removeIf(record -> record.subjectCode.equals(code));
                saveData();
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });

        return panelWithTable(table, new JComponent[]{addButton, updateButton, deleteButton});
    }

    private void subjectDialog(String existingCode) {
        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();

        if (existingCode != null) {
            Subject subject = findSubject(existingCode);
            codeField.setText(subject.getCode());
            codeField.setEditable(false);
            nameField.setText(subject.getName());
        }

        String title = existingCode == null ? "Add Subject" : "Update Subject";
        int choice = JOptionPane.showConfirmDialog(this,
                form(new String[]{"Code", "Name"}, new JComponent[]{codeField, nameField}),
                title, JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        try {
            if (existingCode == null) {
                String code = required(codeField.getText(), "Subject code").toUpperCase();
                for (Subject subject : subjects) {
                    if (subject.getCode().equalsIgnoreCase(code)) {
                        throw new IllegalArgumentException("Subject already exists.");
                    }
                }
                subjects.add(new Subject(code, nameField.getText()));
            } else {
                findSubject(existingCode).setName(nameField.getText());
            }
            saveData();
            refresh();
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    // ---------- FACULTY MANAGEMENT ----------

    private JPanel facultyPanel() {
        DefaultTableModel model = tableModel("ID", "Faculty Name");
        for (Faculty member : faculty) {
            model.addRow(new Object[]{member.getId(), member.getName()});
        }

        JTable table = new JTable(model);
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");

        addButton.addActionListener(e -> facultyDialog(null));
        updateButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) message("Select faculty.");
            else facultyDialog(model.getValueAt(row, 0).toString());
        });
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                message("Select faculty.");
                return;
            }
            try {
                String id = model.getValueAt(row, 0).toString();
                faculty.removeIf(member -> member.getId().equals(id));
                history.removeIf(record -> record.facultyId.equals(id));
                saveData();
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });

        return panelWithTable(table, new JComponent[]{addButton, updateButton, deleteButton});
    }

    private void facultyDialog(String existingId) {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();

        if (existingId != null) {
            Faculty member = findFaculty(existingId);
            idField.setText(member.getId());
            idField.setEditable(false);
            nameField.setText(member.getName());
        }

        String title = existingId == null ? "Add Faculty" : "Update Faculty";
        int choice = JOptionPane.showConfirmDialog(this,
                form(new String[]{"Faculty ID", "Name"}, new JComponent[]{idField, nameField}),
                title, JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        try {
            if (existingId == null) {
                String id = required(idField.getText(), "Faculty ID").toUpperCase();
                for (Faculty member : faculty) {
                    if (member.getId().equalsIgnoreCase(id)) {
                        throw new IllegalArgumentException("Faculty already exists.");
                    }
                }
                faculty.add(new Faculty(id, nameField.getText()));
            } else {
                findFaculty(existingId).setName(nameField.getText());
            }
            saveData();
            refresh();
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    // ---------- ATTENDANCE ----------

    private JPanel attendancePanel() {
        JComboBox<String> studentBox = new JComboBox<>();
        JComboBox<String> subjectBox = new JComboBox<>();
        JComboBox<String> facultyBox = new JComboBox<>();
        JComboBox<Status> statusBox = new JComboBox<>(statusArray);
        JTextField dateField = new JTextField(LocalDate.now().toString());

        for (Student student : students) studentBox.addItem(student.getId());
        for (Subject subject : subjects) subjectBox.addItem(subject.getCode());
        for (Faculty member : faculty) facultyBox.addItem(member.getId());

        JButton recordButton = new JButton("Record / Update");
        recordButton.addActionListener(e -> {
            try {
                if (studentBox.getSelectedItem() == null
                        || subjectBox.getSelectedItem() == null
                        || facultyBox.getSelectedItem() == null) {
                    throw new IllegalArgumentException(
                            "Add student, subject, and faculty records first."
                    );
                }

                LocalDate date;
                try {
                    date = LocalDate.parse(dateField.getText().trim());
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Use date format YYYY-MM-DD.");
                }
                if (date.isAfter(LocalDate.now())) {
                    throw new IllegalArgumentException("Future dates are not allowed.");
                }

                String studentId = studentBox.getSelectedItem().toString();
                String subjectCode = subjectBox.getSelectedItem().toString();
                String facultyId = facultyBox.getSelectedItem().toString();
                Status status = (Status) statusBox.getSelectedItem();

                history.removeIf(record -> record.studentId.equals(studentId)
                        && record.subjectCode.equals(subjectCode)
                        && record.date.equals(date));

                history.add(new Attendance(
                        studentId, subjectCode, facultyId, date, status
                ));
                saveData();
                message("Attendance recorded.");
                refresh();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });

        DefaultTableModel model = tableModel(
                "Student", "Subject", "Faculty", "Date", "Status"
        );
        for (Attendance record : history) {
            model.addRow(new Object[]{record.studentId, record.subjectCode,
                    record.facultyId, record.date, record.status});
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(form(
                new String[]{"Student ID", "Subject", "Faculty", "Date YYYY-MM-DD", "Status", ""},
                new JComponent[]{studentBox, subjectBox, facultyBox, dateField, statusBox, recordButton}
        ), BorderLayout.NORTH);
        panel.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        return panel;
    }

    // ---------- REPORTS ----------

    private JPanel reportPanel() {
        DefaultTableModel model = tableModel(
                "ID", "Name", "Present", "Total", "Percentage", "Remark"
        );
        JComboBox<String> subjectFilter = new JComboBox<>();
        subjectFilter.addItem("ALL");
        for (Subject subject : subjects) subjectFilter.addItem(subject.getCode());

        fillReport(model, sortedByPercentage(null), null);
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        JButton reportButton = new JButton("Generate Report");
        reportButton.addActionListener(e -> {
            String code = selectedSubject(subjectFilter);
            model.setRowCount(0);
            fillReport(model, sortedByPercentage(code), code);
        });

        JButton lowButton = new JButton("Attendance Below 75%");
        lowButton.addActionListener(e -> {
            String code = selectedSubject(subjectFilter);
            ArrayList<Student> lowStudents = new ArrayList<>();

            for (Student student : students) {
                long total = totalRecords(student.getId(), code);
                double percentage = percentage(student.getId(), code);
                if (total > 0 && percentage < 75.0) lowStudents.add(student);
            }

            model.setRowCount(0);
            fillReport(model, lowStudents, code);
        });

        return panelWithTable(table, new JComponent[]{
                new JLabel("Subject"), subjectFilter, reportButton, lowButton,
                new JLabel("Click a column heading to sort")
        });
    }

    private String selectedSubject(JComboBox<String> box) {
        String selected = box.getSelectedItem().toString();
        return selected.equals("ALL") ? null : selected;
    }

    private long totalRecords(String studentId, String subjectCode) {
        long total = 0;
        for (Attendance record : history) {
            boolean sameStudent = record.studentId.equals(studentId);
            boolean sameSubject = subjectCode == null
                    || record.subjectCode.equals(subjectCode);
            if (sameStudent && sameSubject) total++;
        }
        return total;
    }

    private long presentRecords(String studentId, String subjectCode) {
        long present = 0;
        for (Attendance record : history) {
            boolean sameStudent = record.studentId.equals(studentId);
            boolean sameSubject = subjectCode == null
                    || record.subjectCode.equals(subjectCode);
            if (sameStudent && sameSubject && record.status == Status.PRESENT) {
                present++;
            }
        }
        return present;
    }

    private double percentage(String studentId, String subjectCode) {
        long total = totalRecords(studentId, subjectCode);
        if (total == 0) return 0.0;
        return presentRecords(studentId, subjectCode) * 100.0 / total;
    }

    private List<Student> sortedByPercentage(String subjectCode) {
        TreeMap<Double, List<Student>> sorted =
                new TreeMap<>(Comparator.reverseOrder());

        for (Student student : students) {
            double value = percentage(student.getId(), subjectCode);
            sorted.putIfAbsent(value, new ArrayList<>());
            sorted.get(value).add(student);
        }

        ArrayList<Student> result = new ArrayList<>();
        for (List<Student> samePercentage : sorted.values()) {
            result.addAll(samePercentage);
        }
        return result;
    }

    private void fillStudentTable(DefaultTableModel model, List<Student> list) {
        for (Student student : list) {
            model.addRow(new Object[]{student.getId(), student.getName(),
                    student.getClassName(),
                    String.format("%.1f", percentage(student.getId(), null))});
        }
    }

    private void fillReport(DefaultTableModel model, List<Student> list,
                            String subjectCode) {
        for (Student student : list) {
            long total = totalRecords(student.getId(), subjectCode);
            long present = presentRecords(student.getId(), subjectCode);
            double value = percentage(student.getId(), subjectCode);
            String percentageText = total == 0 ? "-" : String.format("%.1f%%", value);
            String remark = total == 0 ? "NO RECORDS" : value < 75 ? "LOW" : "OK";

            model.addRow(new Object[]{student.getId(), student.getName(),
                    present, total, percentageText, remark});
        }
    }

    // ---------- SEARCH METHODS ----------

    private Student findStudent(String id) {
        Student student = studentById.get(id);
        if (student == null) throw new IllegalArgumentException("Student not found.");
        return student;
    }

    private Subject findSubject(String code) {
        for (Subject subject : subjects) {
            if (subject.getCode().equals(code)) return subject;
        }
        throw new IllegalArgumentException("Subject not found.");
    }

    private Faculty findFaculty(String id) {
        for (Faculty member : faculty) {
            if (member.getId().equals(id)) return member;
        }
        throw new IllegalArgumentException("Faculty not found.");
    }

    // ---------- FILE HANDLING ----------

    private void saveData() throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(dataFile))) {
            for (Student student : students) {
                out.println("S\t" + student.getId() + "\t" + student.getName()
                        + "\t" + student.getClassName());
            }
            for (Subject subject : subjects) {
                out.println("U\t" + subject.getCode() + "\t" + subject.getName());
            }
            for (Faculty member : faculty) {
                out.println("F\t" + member.getId() + "\t" + member.getName());
            }
            for (Attendance record : history) {
                out.println("A\t" + record.studentId + "\t" + record.subjectCode
                        + "\t" + record.facultyId + "\t" + record.date
                        + "\t" + record.status);
            }
        }
    }

    private void loadData() {
        if (!dataFile.exists()) return;

        try (BufferedReader in = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("\\t");

                switch (parts[0]) {
                    case "S":
                        Student student = new Student(parts[1], parts[2], parts[3]);
                        students.add(student);
                        studentById.put(student.getId(), student);
                        break;
                    case "U":
                        subjects.add(new Subject(parts[1], parts[2]));
                        break;
                    case "F":
                        faculty.add(new Faculty(parts[1], parts[2]));
                        break;
                    case "A":
                        history.add(new Attendance(parts[1], parts[2], parts[3],
                                LocalDate.parse(parts[4]), Status.valueOf(parts[5])));
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Could not load data: " + ex.getMessage());
        }
    }

    // ---------- GUI HELPERS ----------

    private DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel panelWithTable(JTable table, JComponent[] controls) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JComponent control : controls) top.add(control);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel form(String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i]));
            panel.add(fields[i]);
        }
        return panel;
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    private void error(String text) {
        JOptionPane.showMessageDialog(
                this, text, "Error", JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }

            new AttendanceManagementSystem().setVisible(true);
        });
    }
}
