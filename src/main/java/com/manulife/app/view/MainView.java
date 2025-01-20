package com.manulife.app.view;

import com.manulife.app.entity.User;
import com.manulife.app.repository.UserRepository;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Route("/")
public class MainView extends VerticalLayout {
    private final transient UserRepository userRepository;
    private Grid<User> userGrid;
    private TextField textFieldId;
    private TextField textFieldName;
    private EmailField emailField;

    private Button submitButton;
    private Button deleteButton;
    private Button downloadReportButton;

    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:8086";

    public MainView(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
        setupComponents();
        setupListeners();
    }

    private void setupComponents() {
        textFieldId = new TextField();
        textFieldId.setLabel("id");
        emailField = new EmailField();
        emailField.setLabel("email");
        textFieldName = new TextField();
        textFieldName.setLabel("name");

        final FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.add(textFieldId);
        formLayout.add(emailField);
        formLayout.add(textFieldName);
        formLayout.setMaxWidth(30, Unit.PERCENTAGE);
        setAlignSelf(Alignment.CENTER, formLayout);
        H2 title = new H2("User app");
        setAlignSelf(Alignment.CENTER, title);
        add(title);
        add(formLayout);

        final HorizontalLayout actionPanel = new HorizontalLayout();
        actionPanel.setJustifyContentMode(JustifyContentMode.BETWEEN);
        submitButton = new Button("Submit");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteButton = new Button("Delete");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        downloadReportButton = new Button("Download Report");
        downloadReportButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        actionPanel.add(deleteButton);
        actionPanel.add(downloadReportButton);
        actionPanel.add(submitButton);
        formLayout.add(actionPanel);

        userGrid = new Grid<>(User.class, false);
        userGrid.addColumn(User::getId).setHeader("Id");
        userGrid.addColumn(User::getEmail).setHeader("Email");
        userGrid.addColumn(User::getName).setHeader("Name");

        final List<User> userList = userRepository.findAll();
        userGrid.setItems(userList);

        add(userGrid);
    }

    private void setupListeners() {
        userGrid.addSelectionListener(listener -> {
            final Optional<User> userOptional = listener.getFirstSelectedItem();
            if (userOptional.isPresent()) {
                final User user = userOptional.get();
                textFieldId.setValue(String.valueOf(user.getId()));
                emailField.setValue(user.getEmail());
                textFieldName.setValue(user.getName());
            }
        });

        submitButton.addClickListener(listener -> {
            final Binder<User> userBinder = new Binder<>();
            userBinder.forField(emailField).asRequired().bind(User::getEmail, User::setEmail);
            userBinder.forField(textFieldName).asRequired().bind(User::getName, User::setName);
            final User newUser = new User();
            newUser.setEmail(emailField.getValue());
            newUser.setName(textFieldName.getValue());

            userBinder.setBean(newUser);
            if (userBinder.validate().isOk()) {
                System.out.println("textFieldId.getValue(): " + textFieldId.getValue());
                System.out.println("textFieldId.getValue(): " + textFieldId.getValue());
                System.out.println("textFieldId.getValue(): " + textFieldId.getValue());
                long id = textFieldId.getValue().equals("") ? 1L : Long.valueOf(textFieldId.getValue());
                final Optional<User> existingUser = userRepository.findById(id);
                if (existingUser.isPresent()) {
                    newUser.setId(existingUser.get().getId());
                }
                userRepository.save(newUser);
                userGrid.setItems(userRepository.findAll());
                clearForm();
                showNotification("Success upsert user", NotificationVariant.LUMO_SUCCESS);
            }
        });

        deleteButton.addClickListener(listener -> {
            String id = textFieldId.getValue();
            if (Objects.nonNull(id)) {
                final Optional<User> user = userRepository.findById(Long.valueOf(id));
                if (user.isPresent()) {
                    userRepository.delete(user.get());
                    userGrid.setItems(userRepository.findAll());
                    clearForm();
                    showNotification("Success delete user", NotificationVariant.LUMO_ERROR);
                }
            }
        });

        downloadReportButton.addClickListener(listener -> {
            String id = textFieldId.getValue();
            if (Objects.nonNull(id)) {
                try {
                    ResponseEntity<ByteArrayResource> response = restTemplate.getForEntity(BASE_URL + "/reports/" + id, ByteArrayResource.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        ByteArrayResource resource = response.getBody();
                        if (resource != null) {
                            downloadFile(resource.getByteArray(), "user_report_" + id + ".pdf");
                        }
                    } else {
                        showNotification("Failed to download report", NotificationVariant.LUMO_ERROR);
                    }
                } catch (Exception e) {
                    showNotification("Error: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
                }
            }
        });

    }

    private void downloadFile(byte[] fileContent, String filename) {
        StreamResource resource = new StreamResource(filename, () -> new ByteArrayInputStream(fileContent));
        Anchor downloadLink = new Anchor(resource, "Download Report");
        downloadLink.getElement().setAttribute("download", true);
        add(downloadLink);
        showNotification("Report downloaded: " + filename, NotificationVariant.LUMO_SUCCESS);
    }


    private void showNotification(String msg, NotificationVariant notificationVariant) {
        final Notification notification = new Notification();
        notification.setText(msg);
        notification.setDuration(5000);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.addThemeVariants(notificationVariant);
        notification.open();
    }

    private void clearForm() {
        textFieldId.setValue("");
        emailField.setValue("");
        textFieldName.setValue("");
    }
}
