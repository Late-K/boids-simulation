package uk.ac.gold.boids;

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

        boidsSpinner        = createCountSpinner(params.numberOfBoids(), 0, 9999);
        predatorsSpinner    = createCountSpinner(params.numberOfPredators(), 0, 9999);
        maxSpeedSpinner     = createDoubleSpinner(params.maxSpeed(), 10, 9999, 10);
        maxForceSpinner     = createDoubleSpinner(params.maxForce(), 10, 9999, 10);
        separationSpinner   = createDoubleSpinner(params.separationRadius(), 10, 9999, 5);
        alignSpinner        = createDoubleSpinner(params.alignRadius(), 10, 9999, 5);
        cohesionSpinner     = createDoubleSpinner(params.cohesionRadius(), 10, 9999, 5);
        predatorSpeedSpinner = createDoubleSpinner(params.predatorMaxSpeed(), 10, 9999, 10);
        predatorForceSpinner = createDoubleSpinner(params.predatorMaxForce(), 10, 9999, 10);
        fleeSpinner         = createDoubleSpinner(params.fleeRadius(), 10, 9999, 10);
        chaseSpinner        = createDoubleSpinner(params.predatorChaseRadius(), 10, 9999, 10);

        VBox content = new VBox(5,
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

        Button toggleButton = new Button("Hide");
        toggleButton.setOnAction(e -> {
            boolean nowVisible = !content.isVisible();
            content.setVisible(nowVisible);
            content.setManaged(nowVisible);
            toggleButton.setText(nowVisible ? "Hide" : "Show");
        });

        setMaxWidth(240);
        setMaxHeight(USE_PREF_SIZE);
        setTranslateX(8);
        setTranslateY(48);
        setStyle("-fx-background-color: transparent;");

        getChildren().addAll(toggleButton, content);

        bindLiveParameter(maxSpeedSpinner);
        bindLiveParameter(maxForceSpinner);
        bindLiveParameter(separationSpinner);
        bindLiveParameter(alignSpinner);
        bindLiveParameter(cohesionSpinner);
        bindLiveParameter(predatorSpeedSpinner);
        bindLiveParameter(predatorForceSpinner);
        bindLiveParameter(fleeSpinner);
        bindLiveParameter(chaseSpinner);
        bindCountParameter(boidsSpinner, simulationController::setBoidCount);
        bindCountParameter(predatorsSpinner, simulationController::setPredatorCount);

        refreshStatus();
    }

    private HBox row(String labelText, Spinner<?> spinner) {
        Label label = new Label(labelText);
        label.setMinWidth(80);
        HBox.setHgrow(spinner, Priority.ALWAYS);
        return new HBox(6, label, spinner);
    }

    private HBox actionBar() {
        pauseButton.setMaxWidth(Double.MAX_VALUE);
        pauseButton.setOnAction(event -> {
            simulationController.togglePaused();
            refreshStatus();
        });

        Button resetButton = new Button("Reset");
        resetButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setOnAction(event -> resetSettings());

        HBox bar = new HBox(4, pauseButton, resetButton);
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

    private void bindCountParameter(Spinner<Integer> spinner, java.util.function.IntConsumer setter) {
        spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingControls && newValue != null) {
                commitEditorText(spinner);
                setter.accept(spinner.getValue());
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

    private void refreshStatus() {
        statusLabel.setText(simulationController.isPaused() ? "Paused" : "Running");
        pauseButton.setText(simulationController.isPaused() ? "Resume" : "Pause");
    }
}
