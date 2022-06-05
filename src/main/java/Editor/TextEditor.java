package Editor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TextEditor extends JFrame {

    private JTextArea textArea;
    private JTextField searchField;
    private int currIndex = 0;
    private volatile List<MatchedResult> matchedResults = new ArrayList<>();
    private final JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

    public TextEditor() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 750);
        setBackground(Color.darkGray);
        setLocationRelativeTo(null);
        setTitle("Text Editor");

        initComponents();

        setVisible(true);
    }

    void initComponents() {
        setJMenuBar(initMenuBar());
        initTextField();
        initFileInteractionBtns();
    }

    private void initTextField() {
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setName("TextArea");
        textArea.setFont(new Font("Sans-Serif", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(textArea);
        setLocationRelativeTo(null);
        setMargin(scrollPane, 10, 30, 30, 30);
        add(scrollPane, BorderLayout.CENTER);

    }

    private JMenuBar initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenuItem fileMenu = createMenu("File", new String[] {"Open", "Save", "Exit"});
        JMenuItem searchMenu = createMenu("Search", new String[] {"Search", "Next", "Previous"});
        menuBar.add(fileMenu);
        menuBar.add(searchMenu);
        return menuBar;
    }

    private JMenu createMenu(String menuName, String[] menuItems) {
        JMenu menu = new JMenu(menuName);
//        menu.setMnemonic(KeyEvent.getExtendedKeyCodeForChar(menuName.charAt(0)));
        for (String item : menuItems) {
            createMenuItem(item, menu);
        }
        return menu;
    }

    private void createMenuItem(String text, JMenu menu) {
        JMenuItem item = new JMenuItem(text);
        item.setActionCommand(item.getText());
//        item.setMnemonic(KeyEvent.getExtendedKeyCodeForChar(item.getText().charAt(0)));
        item.addActionListener(this::triggerEvent);

        menu.add(item);
    }

    private void initFileInteractionBtns() {
        JPanel filePanel = new JPanel(new FlowLayout());

        searchField = new JTextField();
        searchField.setName("searchField");
        searchField.setPreferredSize(new Dimension(300, 30));

        JButton saveBtn = createButton("SaveButton", "SaveFile.png", "Save");
        JButton loadBtn = createButton("LoadButton", "OpenFile.png", "Open");
        JButton searchBtn = createButton("SearchButton", "Search.png", "Search");
        JButton previousBtn = createButton("PreviousButton", "PreviousResult.png", "Previous");
        JButton nextBtn = createButton("NextButton", "NextResult.png", "Next");

        filePanel.add(loadBtn);
        filePanel.add(saveBtn);
        filePanel.add(searchField);
        filePanel.add(searchBtn);
        filePanel.add(previousBtn);
        filePanel.add(nextBtn);

        add(filePanel, BorderLayout.NORTH);
    }

    private JButton createButton(String name, String iconFile, String command) {
        JButton button = new JButton(new ImageIcon(getClass().getClassLoader()
                .getResource("images/" + iconFile)));
        button.setName(name);
        button.setActionCommand(command);
        button.addActionListener(this::triggerEvent);
        button.setFocusable(false);

        return button;
    }

    private void setMargin(JComponent component, int top, int left,
                           int bottom, int right) {
        Border border = component.getBorder();

        Border marginBorder = new EmptyBorder(new Insets(top, left, bottom, right));
        component.setBorder(border == null ? marginBorder : new CompoundBorder(marginBorder, border));
    }

    // Responds with the correct action based on the action event
    private void triggerEvent(ActionEvent actionEvent) {
        switch (actionEvent.getActionCommand()) {
            case "Save" -> save();
            case "Open" -> load();
            case "Search" -> search();
            case "Next" -> nextResult();
            case "Previous" -> previousResult();
            case "Exit" -> dispose();
            default -> System.out.println("Invalid event");
        }
    }

    private void save() {
        int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File fileName = jfc.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                textArea.write(writer);
            } catch (IOException e) {
                System.out.println("Write error: " + e.getMessage());
            }
        } else {
            System.out.println("User cancelled the operation: Save");
        }
    }

    private void load() {
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            textArea.setText(null);
            try {
                try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                    textArea.read(reader, null);
                }
            } catch (IOException e) {
                textArea.setText("");
                System.out.println("Read Error: " + e.getMessage());
            }
        } else {
            System.out.println("User cancelled the operation: Load");
        }
    }

    private void search() {
        matchedResults.clear();
        TextSearch search = new TextSearch(searchField.getText(),
                textArea.getText());
        try {
            search.execute();
            matchedResults = search.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!matchedResults.isEmpty()) {
            setCaret(matchedResults.get(0));
        } else {
            System.out.println("The search returned no results.");
        }
    }

    private void nextResult() {
        currIndex++;
        currIndex = currIndex % matchedResults.size();
        MatchedResult result = matchedResults.get(currIndex);
        setCaret(result);
    }

    private void previousResult() {
        currIndex--;
        currIndex = (currIndex + matchedResults.size()) % matchedResults.size();
        MatchedResult result = matchedResults.get(currIndex);
        setCaret(result);

    }

    private void setCaret(MatchedResult result) {
        textArea.setCaretPosition(result.getStartIndex() + result.getText().length());
        textArea.select(result.getStartIndex(),
                result.getStartIndex() + result.getText().length());
        textArea.grabFocus();
    }
}

