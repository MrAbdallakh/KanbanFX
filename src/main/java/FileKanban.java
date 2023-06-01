import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;

public class FileKanban {

    public void saveBoard(ListView<String> toDo, ListView<String> inWork, ListView<String> done, File file) {

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Save Board");
                alert.setContentText("""
                                    The Board can not save because the directory is not accesable
                                    FileKanban.java line 25 has an error!
                                    """);
                alert.show();
            }
        }

        try {
            FileWriter writer = new FileWriter(file);

            for (String word : toDo.getItems()) {
                writer.write(word);
                writer.write("\n-\n");
            }

            writer.flush();
            writer.write("=\n");

            for (String word : inWork.getItems()) {
                writer.write(word);
                writer.write("\n-\n");
            }

            writer.flush();
            writer.write("=\n");

            for (String word : done.getItems()) {
                writer.write(word);
                writer.write("\n-\n");
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            showErrorMessage("File Error", """
                    Probably Problems:

                    *) File is not allow to write.
                    *) You are currently open the file.
                    *) Name is empty.

                    FileKanban.java line 64 has an error!
                    """);
        }
    }

    public LinkedList<String> getLoadBoards(File conf) {
        LinkedList<String> list = new LinkedList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(conf));
            String line;

            while ((line = reader.readLine()) != null) {
                String text = "";

                for (int i = line.length() - 1; line.charAt(i) != '\\'; i++) {
                    text = line.charAt(i) + text;
                }

                list.add(text);
            }

            reader.close();
        } catch (FileNotFoundException e) {
            showErrorMessage("File Error", "File not found!\nFileKanban.java line 88 has an error!");
        } catch (IOException e) {
            showErrorMessage("File Error", "File is not able to read!\nFileKanban.java line 90 has an error!");
        }

        return list;
    }

    public void loadBoard(ListView<String> toDo, ListView<String> inWork, ListView<String> done, File file) {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line, text = "";
            int listIndex = 0; // 0 ... toDo, 1 ... inWork, 2 ... done
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {

                if (!line.equals("-") && !line.equals("=")) {

                    if (firstLine) {
                        text = line;
                        firstLine = false;
                    } else {
                        text = text + "\n" + line;
                    }

                } else if (line.equals("=")) {
                    listIndex++;
                } else {
                    if (!text.equals("")) {
                        switch (listIndex) {
                            case 0:
                                toDo.getItems().add(text);break;
                            case 1:
                                inWork.getItems().add(text);break;
                            case 2:
                                done.getItems().add(text);break;
                        }
                        text = "";
                        firstLine = true;
                    }
                }
            }

            reader.close();
        } catch (FileNotFoundException e) {
            showErrorMessage("File Error", "File is not found!\nFileKanban.java line 135 has an error!");
        } catch (IOException e) {
            showErrorMessage("File Error", "File is not able to read!\nFileKanban.java line 137 has an error!");
        }
    }

    private void showErrorMessage(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}