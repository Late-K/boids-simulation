package uk.ac.gold.boids;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

// settings panel for live simulation controls

public class SimulationControlsPane extends VBox {

    private final SimulationController simulationController;

    private final Spinner<Integer> boidsSpinner;
    private final Spinner<Integer> predatorsSpinner;
    private final Spinner<Double> maxSpeedSpinner;
    private final Spinner<Double> maxForceSpinner;
    private final Spinner<Double> separationSpinner;
    private final Spinner<Double> alignSpinner;
    private final Spinner<Double> cohesionSpinner;
    private final Spinner<Double> predatorSpeedSpinner;
    private final Spinner<Double> predatorForceSpinner;
    private final Spinner<Double> fleeSpinner;
    private final Spinner<Double> chaseSpinner;
    private final Button pauseButton = new Button("Pause");
    private final Label statusLabel = new Label();

    private boolean updatingControls;

    public SimulationControlsPane(SimulationController simulationController) {
        this.simulationController = simulationController;

        SimulationParameters params = simulationController.getParams();

        boidsSpinner        = createCountSpinner(params.numberOfBoids(), 10, 5000);
        predatorsSpinner    = createCountSpinner(params.numberOfPredators(), 0, 50);
        maxSpeedSpinner     = createDoubleSpinner(params.maxSpeed(), 10, 1000, 10);
        maxForceSpinner     = createDoubleSpinner(params.maxForce(), 10, 1000, 10);
        separationSpinner   = createDoubleSpinner(params.separationRadius(), 10, 500, 5);
        alignSpinner        = createDoubleSpinner(params.alignRadius(), 10, 500, 5);
        cohesionSpinner     = createDoubleSpinner(params.cohesionRadius(), 10, 500, 5);
        predatorSpeedSpinner = createDoubleSpinner(params.predatorMaxSpeed(), 10, 1000, 10);
        predatorForceSpinner = createDoubleSpinner(params.predatorMaxForce(), 10, 1000, 10);
        fleeSpinner         = createDoubleSpinner(params.fleeRadius(), 10, 600, 10);
        chaseSpinner        = createDoubleSpinner(params.predatorChaseRadius(), 10, 800, 10);

        setSpacing(4);
        setPadding(new Insets(8));
        setMaxWidth(240);
        setMaxHeight(USE_PREF_SIZE);
        setTranslateX(8);
        setTranslateY(48);
        setStyle("-fx-background-color: transparent;"
                + "-fx-border-color: rgba(255,255,255,0.25);"
                + "-fx-border-width: 1;");

        getChildren().addAll(
                row("Boids",       boidsSpinner),
                row("Predators",   predatorsSpinner),
                row("Boid speed",  maxSpeedSpinner),
                row("Boid force",  maxForceSpinner),
                row("Separation",  separationSpinner),
                row("Align",       alignSpinner),
                row("Cohesion",    cohesionSpinner),
                row("Pred speed",  predatorSpeedSpinner),
                row("Pred force",  predatorForceSpinner),
                row("Flee",        fleeSpinner),
                row("Chase",       chaseSpinner),
                statusLabel,
                actionBar());

        bindLiveParameter(maxSpeedSpinner);
        bindLiveParameter(maxForceSpinner);
        bindLiveParameter(separationSpinner);
        bindLiveParameter(alignSpinner);
        bindLiveParameter(cohesionSpinner);
        bindLiveParameter(predatorSpeedSpinner);
        bindLiveParameter(predatorForceSpinner);
        bindLiveParameter(fleeSpinner);
        bindLiveParameter(chaseSpinner);

        refreshStatus();
    }

    private HBox row(String labelText, Spinner<?> spinner) {
        Label label = new Label(labelText);
        label.setMinWidth(80);
        HBox.setHgrow(spinner, Priority.ALWAYS);
        return new HBox(6, label, spinner);
    }

    private HBox actionBar() {
        Button applyButton = new Button("Apply");
        applyButton.setMaxWidth(Double.MAX_VALUE);
        applyButton.setOnAction(event -> applySettings());

        pauseButton.setMaxWidth(Double.MAX_VALUE);
        pauseButton.setOnAction(event -> {
            simulationController.togglePaused();
            refreshStatus();
        });

        Button resetButton = new Button("Reset");
        resetButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setOnAction(event -> resetSettings());

        HBox bar = new HBox(4, applyButton, pauseButton, resetButton);
        HBox.setHgrow(applyButton, Priority.ALWAYS);
        HBox.setHgrow(pauseButton, Priority.ALWAYS);
        HBox.setHgrow(resetButton, Priority.ALWAYS);
        return bar;
    }

    private Spinner<Integer> createCountSpinner(int initialValue, int minValue, int maxValue) {
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setEditable(true);
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, initialValue));
        spinner.setMaxWidth(Double.MAX_VALUE);
        bindEditorCommit(spinner);
        return spinner;
    }

    private Spinner<Double> createDoubleSpinner(double initialValue, double minValue, double maxValue, double stepSize) {
        Spinner<Double> spinner = new Spinner<>();
        spinner.setEditable(true);
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue, initialValue, stepSize));
        spinner.setMaxWidth(Double.MAX_VALUE);
        bindEditorCommit(spinner);
        return spinner;
    }

    private void bindLiveParameter(Spinner<?> spinner) {
        spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingControls) {
                applyLiveTuning();
            }
        });
    }

    private <T> void bindEditorCommit(Spinner<T> spinner) {
        spinner.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                commitEditorText(spinner);
            }
        });
        spinner.getEditor().setOnAction(event -> commitEditorText(spinner));
    }

    private <T> void commitEditorText(Spinner<T> spinner) {
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory == null) {
            return;
        }
        StringConverter<T> converter = valueFactory.getConverter();
        if (converter == null) {
            return;
        }
        try {
            valueFactory.setValue(converter.fromString(spinner.getEditor().getText()));
        } catch (RuntimeException ex) {
            spinner.getEditor().setText(converter.toString(valueFactory.getValue()));
        }
    }

    private void applyLiveTuning() {
        simulationController.updateTuning(
                maxSpeedSpinner.getValue(),
                maxForceSpinner.getValue(),
                separationSpinner.getValue(),
                alignSpinner.getValue(),
                cohesionSpinner.getValue(),
                predatorSpeedSpinner.getValue(),
                predatorForceSpinner.getValue(),
                fleeSpinner.getValue(),
                chaseSpinner.getValue());
        refreshStatus();
    }

    private void applySettings() {
        commitAllEditors();
        simulationController.applySettings(
                boidsSpinner.getValue(),
                predatorsSpinner.getValue(),
                maxSpeedSpinner.getValue(),
                maxForceSpinner.getValue(),
                separationSpinner.getValue(),
                alignSpinner.getValue(),
                cohesionSpinner.getValue(),
                predatorSpeedSpinner.getValue(),
                predatorForceSpinner.getValue(),
                fleeSpinner.getValue(),
                chaseSpinner.getValue());
        refreshStatus();
    }

    private void resetSettings() {
        SimulationParameters defaults = simulationController.getDefaultParams();

        updatingControls = true;
        boidsSpinner.getValueFactory().setValue(defaults.numberOfBoids());
        predatorsSpinner.getValueFactory().setValue(defaults.numberOfPredators());
        maxSpeedSpinner.getValueFactory().setValue(defaults.maxSpeed());
        maxForceSpinner.getValueFactory().setValue(defaults.maxForce());
        separationSpinner.getValueFactory().setValue(defaults.separationRadius());
        alignSpinner.getValueFactory().setValue(defaults.alignRadius());
        cohesionSpinner.getValueFactory().setValue(defaults.cohesionRadius());
        predatorSpeedSpinner.getValueFactory().setValue(defaults.predatorMaxSpeed());
        predatorForceSpinner.getValueFactory().setValue(defaults.predatorMaxForce());
        fleeSpinner.getValueFactory().setValue(defaults.fleeRadius());
        chaseSpinner.getValueFactory().setValue(defaults.predatorChaseRadius());
        updatingControls = false;

        simulationController.reset();
        refreshStatus();
    }

    private void commitAllEditors() {
        commitEditorText(boidsSpinner);
        commitEditorText(predatorsSpinner);
        commitEditorText(maxSpeedSpinner);
        commitEditorText(maxForceSpinner);
        commitEditorText(separationSpinner);
        commitEditorText(alignSpinner);
        commitEditorText(cohesionSpinner);
        commitEditorText(predatorSpeedSpinner);
        commitEditorText(predatorForceSpinner);
        commitEditorText(fleeSpinner);
        commitEditorText(chaseSpinner);
    }

    private void refreshStatus() {
        statusLabel.setText(simulationController.isPaused() ? "Paused" : "Running");
        pauseButton.setText(simulationController.isPaused() ? "Resume" : "Pause");
    }
}
