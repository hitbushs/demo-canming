package org.example.democanting;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class RestaurantReservationApp extends Application {

    private UserManager userManager = new UserManager();
    private RestaurantManager restaurantManager = new RestaurantManager();
    private ReservationManager reservationManager = new ReservationManager();
    private int currentUserId = -1; // 用于存储当前登录用户的ID
    private boolean isAdmin = false; // 用于存储当前用户是否为管理员
    private TabPane tabPane;
    private Tab loginTab; // 定义loginTab变量

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("餐厅预订系统");

        tabPane = new TabPane();

        // 登录和注册选项卡
        loginTab = new Tab("登录/注册", createLoginRegisterPane(primaryStage));
        loginTab.setClosable(false);

        // 餐厅列表和预订选项卡
        Tab restaurantTab = new Tab("餐厅列表", createRestaurantPane());
        restaurantTab.setClosable(false);

        // 预订记录选项卡
        Tab reservationTab = new Tab("预订记录", createReservationPane());
        reservationTab.setClosable(false);

        // 用户设置选项卡
        Tab userSettingsTab = new Tab("用户设置", createUserSettingsPane());
        userSettingsTab.setClosable(false);

        // 管理员功能选项卡
        Tab adminTab = new Tab("管理员功能", createAdminPane());
        adminTab.setClosable(false);

        tabPane.getTabs().addAll(loginTab, restaurantTab, reservationTab, userSettingsTab, adminTab);

        // 默认禁用非登录选项卡
        restaurantTab.setDisable(true);
        reservationTab.setDisable(true);
        userSettingsTab.setDisable(true);
        adminTab.setDisable(true);

        Scene scene = new Scene(tabPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Pane createLoginRegisterPane(Stage primaryStage) {
        Label titleLabel = new Label("欢迎来到餐厅预订系统");
        titleLabel.setFont(new Font("Arial", 28));
        titleLabel.setTextFill(Color.DARKSLATEBLUE);

        Label userLabel = new Label("用户名:");
        TextField userTextField = new TextField();
        userTextField.setPromptText("请输入用户名");

        Label passwordLabel = new Label("密码:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");

        Button loginButton = new Button("登录");
        loginButton.setOnAction(e -> {
            String username = userTextField.getText();
            String password = passwordField.getText();
            int userId = userManager.login(username, password);
            if (userId != -1) {
                currentUserId = userId;
                isAdmin = userManager.isAdmin(userId);
                showAlert(Alert.AlertType.INFORMATION, "登录成功", "欢迎回来，" + username + "!");
                enableTabsAfterLogin();
                tabPane.getTabs().remove(loginTab); // 移除登录选项卡
            } else {
                showAlert(Alert.AlertType.ERROR, "登录失败", "用户名或密码错误！");
            }
        });

        Button registerButton = new Button("注册");
        registerButton.setOnAction(e -> {
            String username = userTextField.getText();
            String password = passwordField.getText();
            int userId = userManager.register(username, password);
            if (userId != -1) {
                currentUserId = userId;
                showAlert(Alert.AlertType.INFORMATION, "注册成功", "欢迎，" + username + "!");
                enableTabsAfterLogin();
                tabPane.getTabs().remove(loginTab); // 移除登录选项卡
            } else {
                showAlert(Alert.AlertType.ERROR, "注册失败", "用户名已存在或其他错误！");
            }
        });

        Button logoutButton = new Button("注销");
        logoutButton.setOnAction(e -> {
            currentUserId = -1;
            isAdmin = false;
            disableTabsAfterLogout();
            tabPane.getTabs().add(0, loginTab); // 重新添加登录选项卡
            showAlert(Alert.AlertType.INFORMATION, "注销成功", "您已成功注销");
        });

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.add(userLabel, 0, 0);
        gridPane.add(userTextField, 1, 0);
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);
        gridPane.add(loginButton, 1, 2);
        gridPane.add(registerButton, 0, 2);
        gridPane.add(logoutButton, 0, 3, 2, 1);

        VBox layout = new VBox(20, titleLabel, gridPane);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        return layout;
    }

    private void enableTabsAfterLogin() {
        tabPane.getTabs().get(0).setDisable(false); // 餐厅列表
        tabPane.getTabs().get(1).setDisable(false); // 预订记录
        tabPane.getTabs().get(2).setDisable(false); // 用户设置
        if (isAdmin) {
            tabPane.getTabs().get(3).setDisable(false); // 管理员功能
        }
    }

    private void disableTabsAfterLogout() {
        tabPane.getTabs().get(0).setDisable(true); // 餐厅列表
        tabPane.getTabs().get(1).setDisable(true); // 预订记录
        tabPane.getTabs().get(2).setDisable(true); // 用户设置
        tabPane.getTabs().get(3).setDisable(true); // 管理员功能
    }

    private Pane createRestaurantPane() {
        List<Restaurant> restaurants = restaurantManager.getRestaurants();
        ListView<Restaurant> restaurantListView = new ListView<>();
        restaurantListView.getItems().addAll(restaurants);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> timePicker = new ComboBox<>();
        timePicker.getItems().addAll("12:00", "13:00", "14:00", "18:00", "19:00", "20:00");

        Button reserveButton = new Button("预订");
        reserveButton.setOnAction(e -> {
            Restaurant selectedRestaurant = restaurantListView.getSelectionModel().getSelectedItem();
            LocalDate date = datePicker.getValue();
            String time = timePicker.getValue();
            if (selectedRestaurant != null && date != null && time != null) {
                reservationManager.makeReservation(currentUserId, selectedRestaurant.getId(), date.toString(), time, 2);
                showAlert(Alert.AlertType.INFORMATION, "预订成功", "您已成功预订 " + selectedRestaurant.getName());
                updateReservationList(); // 更新预订记录
            } else {
                showAlert(Alert.AlertType.WARNING, "预订失败", "请选择餐厅、日期和时间进行预订！");
            }
        });

        VBox layout = new VBox(10, restaurantListView, datePicker, timePicker, reserveButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        return layout;
    }

    private Pane createReservationPane() {
        ListView<String> reservationListView = new ListView<>();
        updateReservationList(reservationListView);

        Button cancelButton = new Button("取消预订");
        cancelButton.setOnAction(e -> {
            String selectedReservation = reservationListView.getSelectionModel().getSelectedItem();
            if (selectedReservation != null) {
                int reservationId = extractReservationId(selectedReservation);
                reservationManager.cancelReservation(reservationId);
                reservationListView.getItems().remove(selectedReservation);
                showAlert(Alert.AlertType.INFORMATION, "取消成功", "您已成功取消预订");
            } else {
                showAlert(Alert.AlertType.WARNING, "取消失败", "请选择一个预订进行取消！");
            }
        });

        VBox layout = new VBox(10, reservationListView, cancelButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        return layout;
    }

    private void updateReservationList(ListView<String> reservationListView) {
        List<String> reservations = reservationManager.getReservationsByUserId(currentUserId);
        reservationListView.getItems().setAll(reservations);
    }

    private Pane createUserSettingsPane() {
        if (currentUserId == -1) {
            return new VBox(new Label("请先登录"));
        }

        TextField usernameField = new TextField();
        usernameField.setPromptText("新用户名");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("新密码");

        Button updateInfoButton = new Button("更新信息");
        updateInfoButton.setOnAction(e -> {
            String newUsername = usernameField.getText();
            String newPassword = newPasswordField.getText();
            if (userManager.updateUserInfo(currentUserId, newUsername, newPassword)) {
                showAlert(Alert.AlertType.INFORMATION, "更新成功", "用户信息已更新");
            } else {
                showAlert(Alert.AlertType.ERROR, "更新失败", "无法更新用户信息");
            }
        });

        VBox layout = new VBox(10, usernameField, newPasswordField, updateInfoButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        return layout;
    }

    private Pane createAdminPane() {
        if (!isAdmin) {
            return new VBox(new Label("您没有权限访问此功能"));
        }

        ListView<Restaurant> restaurantListView = new ListView<>();
        restaurantListView.getItems().addAll(restaurantManager.getRestaurants());

        TextField restaurantNameField = new TextField();
        restaurantNameField.setPromptText("餐厅名称");

        TextField restaurantAddressField = new TextField();
        restaurantAddressField.setPromptText("餐厅地址");

        TextField restaurantPhoneField = new TextField();
        restaurantPhoneField.setPromptText("餐厅电话");

        Button updateRestaurantButton = new Button("更新餐厅");
        updateRestaurantButton.setOnAction(e -> {
            Restaurant selectedRestaurant = restaurantListView.getSelectionModel().getSelectedItem();
            if (selectedRestaurant != null) {
                String name = restaurantNameField.getText();
                String address = restaurantAddressField.getText();
                String phone = restaurantPhoneField.getText();
                if (restaurantManager.updateRestaurant(selectedRestaurant.getId(), name, address, phone)) {
                    showAlert(Alert.AlertType.INFORMATION, "更新成功", "餐厅信息已更新");
                } else {
                    showAlert(Alert.AlertType.ERROR, "更新失败", "无法更新餐厅信息");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "选择错误", "请选择一个餐厅进行更新");
            }
        });

        Button deleteRestaurantButton = new Button("删除餐厅");
        deleteRestaurantButton.setOnAction(e -> {
            Restaurant selectedRestaurant = restaurantListView.getSelectionModel().getSelectedItem();
            if (selectedRestaurant != null) {
                if (restaurantManager.deleteRestaurant(selectedRestaurant.getId())) {
                    restaurantListView.getItems().remove(selectedRestaurant);
                    showAlert(Alert.AlertType.INFORMATION, "删除成功", "餐厅已删除");
                } else {
                    showAlert(Alert.AlertType.ERROR, "删除失败", "无法删除餐厅");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "选择错误", "请选择一个餐厅进行删除");
            }
        });

        Button exportReservationsButton = new Button("导出预订记录");
        exportReservationsButton.setOnAction(e -> {
            List<String> allReservations = reservationManager.getAllReservations();
            try (FileWriter writer = new FileWriter("reservations.csv")) {
                for (String reservation : allReservations) {
                    writer.write(reservation + "\n");
                }
                showAlert(Alert.AlertType.INFORMATION, "导出成功", "预订记录已导出到reservations.csv");
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "导出失败", "无法导出预订记录");
            }
        });

        VBox layout = new VBox(10, restaurantListView, restaurantNameField, restaurantAddressField, restaurantPhoneField, updateRestaurantButton, deleteRestaurantButton, exportReservationsButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        return layout;
    }

    private int extractReservationId(String reservationInfo) {
        String[] parts = reservationInfo.split(",");
        return Integer.parseInt(parts[0].split(":")[1].trim());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}