package id.maulidya.notetaking.Model;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import com.google.firebase.database.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;

public class NoteController {
    @FXML
    private TreeView<String> treeView; // TreeView untuk menampilkan struktur catatan
    @FXML
    private HTMLEditor htmlEditor; // HTMLEditor untuk mengedit isi catatan
    @FXML
    private Button saveNoteButton; // Tombol untuk menyimpan catatan
    @FXML
    private Button editNoteButton; // Tombol untuk mengedit catatan
    private Map<String, String> notes = new HashMap<>(); // Peta untuk menyimpan catatan
    private String userId; // ID pengguna
    private ValueEventListener noteListener; // Listener untuk Firebase
    @FXML
    private Label noteDetailsLabel; // Label untuk menampilkan detail catatan

    @FXML
    public void initialize() {
        initializeTreeView();
        htmlEditor.setVisible(false); // Menyembunyikan HTMLEditor saat pertama kali
        saveNoteButton.setVisible(false); // Menyembunyikan tombol simpan saat pertama kali
        editNoteButton.setVisible(false); // Menyembunyikan tombol edit saat pertama kali

        // Menambahkan listener untuk TreeView
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.getValue().equals("Root")) {
                updateNoteDetailsLabel(newValue); // Memperbarui label detail catatan
            } else {
                noteDetailsLabel.setText("Tidak ada item terpilih"); // Pesan jika tidak ada item terpilih
            }
        });

        // Menangani klik ganda pada TreeView
        treeView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                handleTreeViewDoubleClick();
            }
        });

        // Menangani permintaan menu konteks pada TreeView
        treeView.setOnContextMenuRequested(this::showContextMenu);
    }

    // Memperbarui label detail catatan
    private void updateNoteDetailsLabel(TreeItem<String> selectedItem) {
        String itemName = selectedItem.getValue();
        if (itemName != null && !itemName.isEmpty()) {
            noteDetailsLabel.setText(itemName); // Menampilkan nama item
        } else {
            noteDetailsLabel.setText("Tidak ada nama");
        }
    }

    // Mendapatkan jalur penuh dari item yang dipilih dalam TreeView
    private String getFullPath(TreeItem<String> item) {
        StringBuilder fullPath = new StringBuilder(item.getValue());
        TreeItem<String> parent = item.getParent();
        while (parent != null && !parent.getValue().equals("Root")) {
            fullPath.insert(0, parent.getValue() + "/");
            parent = parent.getParent();
        }
        return fullPath.toString();
    }

    // Mengatur ID pengguna
    public void setUserId(String userId) {
        this.userId = userId;
        initializeFirebaseListener(); // Inisialisasi listener Firebase setelah ID pengguna diatur
    }

    // Menginisialisasi TreeView
    private void initializeTreeView() {
        TreeItem<String> rootItem = new TreeItem<>("Root");
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem); // Mengatur root item pada TreeView
        treeView.setShowRoot(false); // Menyembunyikan root item

        // Mengatur tampilan sel TreeView
        treeView.setCellFactory(tv -> new TreeCell<String>() {
            private final HBox hBox = new HBox(); // HBox untuk mengatur label
            private final Label label = new Label(); // Label untuk menampilkan teks

            {
                HBox.setHgrow(label, Priority.ALWAYS); // Mengatur agar label selalu tumbuh
                label.setMaxWidth(Double.MAX_VALUE);
                hBox.setAlignment(Pos.CENTER_LEFT); // Mengatur posisi label
                hBox.getChildren().add(label); // Menambahkan label ke HBox
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    label.setText(item);
                    if (getTreeItem() != null && !getTreeItem().isLeaf()) {
                        label.setStyle("-fx-background-color: #F3F4EE;");
                    } else {
                        label.setStyle("");
                    }
                    setGraphic(hBox);
                    setText(null);
                }
            }
        });
    }

    // Menginisialisasi listener Firebase untuk memuat catatan
    private void initializeFirebaseListener() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notes").child(userId);
        noteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notes.clear(); // Membersihkan catatan sebelumnya
                treeView.getRoot().getChildren().clear(); // Membersihkan TreeView sebelumnya
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    String title = noteSnapshot.getKey();
                    if (noteSnapshot.hasChildren()) {
                        TreeItem<String> folderItem = new TreeItem<>(title);
                        treeView.getRoot().getChildren().add(folderItem);
                        addSubItems(folderItem, noteSnapshot); // Menambahkan subitem jika ada
                    } else {
                        String content = noteSnapshot.getValue(String.class);
                        notes.put(title, content); // Menyimpan konten catatan
                        TreeItem<String> noteItem = new TreeItem<>(title);
                        treeView.getRoot().getChildren().add(noteItem);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Gagal membaca data: " + databaseError.getCode());
            }
        };
        ref.addValueEventListener(noteListener); // Menambahkan listener ke referensi Firebase
    }

    // Menambahkan subitem ke TreeView
    private void addSubItems(TreeItem<String> parent, DataSnapshot snapshot) {
        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
            String childTitle = childSnapshot.getKey();
            if (childSnapshot.hasChildren()) {
                TreeItem<String> subFolder = new TreeItem<>(childTitle);
                parent.getChildren().add(subFolder);
                addSubItems(subFolder, childSnapshot); // Rekursif untuk menambahkan subitem
            } else {
                String content = childSnapshot.getValue(String.class);
                notes.put(getFullPath(parent) + "/" + childTitle, content); // Menyimpan konten catatan
                TreeItem<String> noteItem = new TreeItem<>(childTitle);
                parent.getChildren().add(noteItem);
            }
        }
    }

    // Menangani penyimpanan catatan
    @FXML
    private void handleSaveNote() {
        TreeItem<String> selectedNote = treeView.getSelectionModel().getSelectedItem();
        if (selectedNote != null && selectedNote.getParent() != null) {
            String noteContent = htmlEditor.getHtmlText();
            notes.put(getFullPath(selectedNote), noteContent);

            DatabaseReference noteRef = FirebaseDatabase.getInstance().getReference("notes").child(userId).child(getFullPath(selectedNote));
            noteRef.setValue(noteContent, (databaseError, databaseReference) -> {
                Platform.runLater(() -> {
                    if (databaseError == null) {
                        showAlert("Simpan", "Catatan '" + selectedNote.getValue() + "' berhasil disimpan!");
                        expandToNode(selectedNote); // Mengembangkan node yang dipilih
                        saveNoteButton.setVisible(false);
                        editNoteButton.setVisible(true);
                    } else {
                        showAlert("Simpan Catatan", "Gagal menyimpan catatan: " + databaseError.getMessage());
                    }
                });
            });
        }
    }

    // Menangani pengeditan catatan
    @FXML
    private void handleEditNote() {
        htmlEditor.setDisable(false); // Mengaktifkan editor
        saveNoteButton.setVisible(true);
        editNoteButton.setVisible(false);
    }

    // Menangani pembuatan folder utama
    @FXML
    private void handleMainFolder() {
        TextInputDialog dialog = new TextInputDialog("Nama Folder");
        dialog.setTitle("Folder Baru");
        dialog.setHeaderText("Masukkan Nama Folder:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                TreeItem<String> newFolder = new TreeItem<>(name);
                treeView.getRoot().getChildren().add(newFolder);

                DatabaseReference folderRef = FirebaseDatabase.getInstance().getReference("notes").child(userId).child(name);
                folderRef.setValue(new HashMap<String, Object>(), (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        showAlert("Error", "Gagal menyimpan folder: " + databaseError.getMessage());
                    }
                });
            }
        });
    }

    // Menangani pembuatan subfolder dalam folder
    @FXML
    private void handleNewFolderInFolder(TreeItem<String> selectedItem) {
        TextInputDialog dialog = new TextInputDialog("Nama SubFolder");
        dialog.setTitle("SubFolder Baru");
        dialog.setHeaderText("Masukkan Nama SubFolder:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                TreeItem<String> newFolder = new TreeItem<>(name);
                selectedItem.getChildren().add(newFolder);
                selectedItem.setExpanded(true);
                treeView.getSelectionModel().select(newFolder);

                String fullPath = getFullPath(newFolder);
                DatabaseReference folderRef = FirebaseDatabase.getInstance().getReference("notes").child(userId).child(fullPath);

                folderRef.setValue(new HashMap<String, Object>(), (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        showAlert("Error", "Gagal menyimpan folder: " + databaseError.getMessage());
                    }
                });
            }
        });
    }

    // Menangani pembuatan catatan baru dalam folder
    @FXML
    private void handleNewNoteInFolder(TreeItem<String> selectedFolder) {
        TextInputDialog dialog = new TextInputDialog("Judul Catatan");
        dialog.setTitle("Catatan Baru");
        dialog.setHeaderText("Masukkan Judul Catatan:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(title -> {
            if (!title.trim().isEmpty()) {
                TreeItem<String> newNote = new TreeItem<>(title);
                selectedFolder.getChildren().add(newNote);
                selectedFolder.setExpanded(true);
                treeView.getSelectionModel().select(newNote);
                notes.put(getFullPath(newNote), "");

                htmlEditor.setHtmlText("");
                htmlEditor.setVisible(true);
                saveNoteButton.setVisible(false);
                editNoteButton.setVisible(true);

                DatabaseReference noteRef = FirebaseDatabase.getInstance().getReference("notes").child(userId).child(getFullPath(newNote));
                noteRef.setValue("", (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        showAlert("Error", "Gagal menyimpan catatan: " + databaseError.getMessage());
                    }
                });
            }
        });
    }

    // Menangani klik ganda pada TreeView untuk mengedit catatan
    private void handleTreeViewDoubleClick() {
        TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.getValue().equals("Root") && selectedItem.isLeaf()) {
            String fullPath = getFullPath(selectedItem);
            if (notes.containsKey(fullPath)) {
                htmlEditor.setHtmlText(notes.get(fullPath));
                htmlEditor.setDisable(true);
                htmlEditor.setVisible(true);
                saveNoteButton.setVisible(false);
                editNoteButton.setVisible(true);
            } else {
                htmlEditor.setHtmlText("");
                htmlEditor.setVisible(false);
                saveNoteButton.setVisible(false);
                editNoteButton.setVisible(false);
            }
        } else {
            htmlEditor.setHtmlText("");
            htmlEditor.setVisible(false);
            saveNoteButton.setVisible(false);
            editNoteButton.setVisible(false);
        }
    }

    // Menangani logout pengguna
    @FXML
    private void handleLogout() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/id.maulidya.notetaking/login.fxml"));
        Stage stage = (Stage) treeView.getScene().getWindow();
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }

    // Mengembangkan node pada TreeView
    private void expandToNode(TreeItem<String> item) {
        if (item.getParent() != null) {
            expandToNode(item.getParent());
        }
        item.setExpanded(true);
    }

    // Menampilkan menu konteks pada TreeView
    private void showContextMenu(ContextMenuEvent event) {
        TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            ContextMenu contextMenu = new ContextMenu();

            MenuItem newNoteMenuItem = new MenuItem("\uD83D\uDDD2 Catatan Baru");
            newNoteMenuItem.setOnAction(e -> handleNewNoteInFolder(selectedItem));

            MenuItem newFolderMenuItem = new MenuItem("\uD83D\uDCC1 SubFolder Baru");
            newFolderMenuItem.setOnAction(e -> handleNewFolderInFolder(selectedItem));

            MenuItem renameMenuItem = new MenuItem("\uD83D\uDCDD Ganti Nama");
            renameMenuItem.setOnAction(e -> handleRename(selectedItem));

            MenuItem deleteMenuItem = new MenuItem("\uD83D\uDDD1 Hapus");
            deleteMenuItem.setOnAction(e -> handleDelete());

            contextMenu.getItems().addAll(newNoteMenuItem, newFolderMenuItem, deleteMenuItem, renameMenuItem);
            contextMenu.show(treeView, event.getScreenX(), event.getScreenY());
        }
    }

    // Menangani penghapusan item pada TreeView
    private void handleDelete() {
        TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.getValue().equals("Root")) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Menghapus Item");
            confirmationAlert.setHeaderText("Apakah kamu yakin akan menghapus " + selectedItem.getValue() + "?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String fullPath = getFullPath(selectedItem);
                DatabaseReference noteRef = FirebaseDatabase.getInstance().getReference("notes").child(userId).child(fullPath);

                noteRef.removeValue((databaseError, databaseReference) -> {
                    if (databaseError == null) {
                        removeTreeItem(selectedItem);
                        showAlert("Menghapus Item", "Item '" + selectedItem.getValue() + "' berhasil dihapus!.");
                    } else {
                        showAlert("Menghapus Item", "Gagal menghapus item: " + databaseError.getMessage());
                    }
                });
            }
        }
    }

    // Menghapus item dari TreeView
    private void removeTreeItem(TreeItem<String> item) {
        String fullPath = getFullPath(item);
        notes.remove(fullPath);

        for (TreeItem<String> child : item.getChildren()) {
            removeTreeItem(child);
        }

        TreeItem<String> parent = item.getParent();
        if (parent != null) {
            parent.getChildren().remove(item);
        }
    }

    // Menampilkan pesan alert
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Menangani penggantian nama item pada TreeView
    private void handleRename(TreeItem<String> selectedItem) {
        TextInputDialog dialog = new TextInputDialog(selectedItem.getValue());
        dialog.setTitle("Ganti Nama Item");
        dialog.setHeaderText("Masukkan nama baru:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.equals(selectedItem.getValue())) {
                String oldPath = getFullPath(selectedItem);
                String newPath = oldPath.substring(0, oldPath.lastIndexOf("/") + 1) + newName;

                DatabaseReference oldRef = FirebaseDatabase.getInstance().getReference("notes").child(userId).child(oldPath);
                DatabaseReference newRef = FirebaseDatabase.getInstance().getReference("notes").child(userId).child(newPath);

                oldRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            newRef.setValue(snapshot.getValue(), (error, ref) -> {
                                if (error == null) {
                                    oldRef.removeValue((removeError, removeRef) -> {
                                        if (removeError == null) {
                                            selectedItem.setValue(newName);
                                            String oldContent = notes.remove(oldPath);
                                            if (oldContent != null) {
                                                notes.put(newPath, oldContent);
                                            }
                                        } else {
                                            showAlert("Ganti Nama Item", "Gagal mengganti nama item: " + removeError.getMessage());
                                        }
                                    });
                                } else {
                                    showAlert("Ganti Nama Item", "Gagal mengganti nama item: " + error.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        showAlert("Rename Item", "Gagal mengganti nama item: " + error.getMessage());
                    }
                });
            }
        });
    }
}
