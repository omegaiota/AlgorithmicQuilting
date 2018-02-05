package jackiequiltpatterndeterminaiton;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static jackiequiltpatterndeterminaiton.Main.SkeletonPathType.*;
import static jackiequiltpatterndeterminaiton.Main.SkeletonRenderType.*;

public class Main extends Application {
    /* Constant */
    private static final int columnItemSpacing = 15, columnItemBundleSpacing = 3;
    /* Labels */
    private final Label textFieldLabel = new Label(), patternRenderFieldLabel = new Label("Repetitions"),
            skeletonGenFieldLabel = new Label("Rows"), regionLabel = new Label("Region"),
            regionSelectionLabel = new Label("Select Region"), skeletonLabel = new Label("Skeleton Path"),
            skeletonGnerationlabel = new Label("Skeleton Path Generation"), patternLabel = new Label("Pattern"),
            patternSelectionLabel = new Label("Select Pattern"), skeletonRenderinglabel = new Label("Skeleton Path Rendering"),
            patternRenderLabel = new Label("Pattern Rendering"), toolLabel = new Label("Tools"),
            svgToPatLabel = new Label(".SVG to .PAT"), quiltingPatternGeneration = new Label("Quilting Pattern Generation"),
            skeletonRenderFieldLabel = new Label("Decoration Density");
    /* Toggle Group */
    private final ToggleGroup patternSourceGroup = new ToggleGroup();
    /* Buttons */
    private final Button loadRegionButton = new Button("Load region ...");
    private final Button loadDecoElementButton = new Button("Load a pattern file ...");
    private final Button loadSvgFileButton = new Button("Load SVG file");
    private final Button generateButton = new Button("Generate");
    List<String> tileList = new ArrayList<>(), alongPathList = new ArrayList<>(), endpointList = new ArrayList<>();
    private PatternRenderer patternRenderer;
    /* Layout: VBox, HBox*/
    //Column
    private VBox regionColumn = new VBox(columnItemSpacing), skeletonColumn = new VBox(columnItemSpacing),
            patternColumn = new VBox(columnItemSpacing), toolColumn = new VBox(columnItemSpacing);
    //Label + item
    private VBox regionSelection = new VBox(columnItemBundleSpacing), skeletonGeneration = new VBox(columnItemBundleSpacing),
            skeletonRendering = new VBox(columnItemBundleSpacing), patternRendering = new VBox(columnItemBundleSpacing),
            patternPropertyInput = new VBox(columnItemBundleSpacing), svgToPat = new VBox(columnItemBundleSpacing),
            skeletonGenPropertyInput = new VBox(columnItemBundleSpacing), skeletonRenderPropertyInput = new VBox(columnItemBundleSpacing),
            patternSelection = new VBox(columnItemBundleSpacing);
    private HBox menu = new HBox(15), patternSourceBox = new HBox(2), fileSourceBox = new HBox(2);
    //ComboBox
    private ComboBox skeletonGenComboBox = new ComboBox(), skeletonRenderComboBox = new ComboBox(),
            patternLibraryComboBox = new ComboBox(), patternRenderComboBox = new ComboBox();
    /* Fonts */
    private Font columnLabelFont = new Font("Luminari", 22), functionLabelFont = new Font("Avenir Light", 14),
            buttonFont = new Font("Avenir", 10), titleFont = new Font("Luminari", 40);
    private Color labelColor = Color.ORANGE, columnLabelColor = Color.SILVER;
    private ToggleButton patternFromFile = new ToggleButton("from file"), noPattern = new ToggleButton("none"),
            patternFromLibrary = new ToggleButton("from library");
    /* Folder */
    private File tileLibrary = new File("./src/resources/patterns/tiles/"),
            alongPathLibrary = new File("./src/resources/patterns/alongPath/"),
            endpointLibrary = new File("./src/resources/patterns/endpoints/");
    /* TextField */
    private TextField patternRenderTextFiled = new TextField(), skeletonGenTextField = new TextField(),
            skeletonRenderTextField = new TextField();
    /* File processor, renderer */
    private SvgFileProcessor decoElementFile = null, regionFile, svgFile;
    private GenerationInfo info = new GenerationInfo();
    private SpinePatternMerger mergedPattern;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Quilt Pattern Generation");
        stage.setScene(new Scene(setLayoutWithGraph(stage), Color.rgb(35, 39, 50)));
        stage.show();
        setDefaultValue();
    }

    private void setDefaultValue() {
        skeletonGenComboBox.setValue(THREE_3_4_3_4_TESSELLATION);
        skeletonRenderTextField.setText("0");
        skeletonGenTextField.setText("30");
        skeletonRenderComboBox.setValue(PEBBLE);
        patternRenderComboBox.setValue("No Rendering");
        patternFromLibrary.setSelected(true);
    }

    public BorderPane setLayoutWithGraph(Stage stage) {
        BorderPane layout = new BorderPane();
        setUpFont();

        //Region
        /* Region Selection*/
        regionSelection.getChildren().addAll(regionSelectionLabel, loadRegionButton);
        regionColumn.getChildren().addAll(regionLabel, regionSelection);

        //Skeleton
        /* Skeleton Path Generation */
        skeletonGenComboBox.getItems().addAll(SkeletonPathType.values());
        skeletonGeneration.getChildren().addAll(skeletonGnerationlabel, skeletonGenComboBox);

        /* Skeleton Path Rendering */
        skeletonRenderComboBox.getItems().addAll(SkeletonRenderType.values());
        skeletonRendering.getChildren().addAll(skeletonRenderinglabel, skeletonRenderComboBox);

        skeletonColumn.getChildren().addAll(skeletonLabel, skeletonGeneration, skeletonGenPropertyInput, skeletonRendering, skeletonRenderPropertyInput);

        //Pattern Column

        /* Pattern selection*/

        /*  Toggle group*/
        patternSourceGroup.getToggles().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternSourceBox.getChildren().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternSelection.getChildren().addAll(patternSelectionLabel, patternSourceBox, fileSourceBox);

        /* initialize pattern library */
        addLibraryFilesToList(tileLibrary, tileList);
        addLibraryFilesToList(alongPathLibrary, alongPathList);
        addLibraryFilesToList(endpointLibrary, endpointList);


        System.out.println(endpointList.size() + " " + alongPathList.size() + " " + tileList.size());
        /* Pattern rendering */
        patternRenderComboBox.getItems().addAll("Repeat with Rotation", "Echo", "No Rendering");


        patternRendering.getChildren().setAll(patternRenderLabel, patternRenderComboBox);
        patternColumn.getChildren().addAll(patternLabel, patternSelection, patternRendering, patternPropertyInput);


        //Tool Column
        //svgToPat
        svgToPat.getChildren().addAll(svgToPatLabel, loadSvgFileButton);
        toolColumn.getChildren().addAll(toolLabel, svgToPat);
        menu.getChildren().addAll(regionColumn, skeletonColumn, patternColumn, toolColumn);

        layout.setCenter(menu);
        layout.setBottom(generateButton);
        layout.setTop(quiltingPatternGeneration);

        layout.setPadding(new Insets(60));
        BorderPane.setAlignment(generateButton, Pos.BOTTOM_CENTER);
        BorderPane.setAlignment(quiltingPatternGeneration, Pos.TOP_CENTER);
        BorderPane.setMargin(generateButton, new Insets(10, 8, 8, 8));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");

        //Listeners
        setupListeners();

        //Buttons, File loader
        buttonActions(stage);
        return layout;
    }

    private void addLibraryFilesToList(File folderFile, List<String> fileList) {
        for (File tileFile : folderFile.listFiles()) {
            String fileName = tileFile.getName();
            fileName = fileName.substring(0, fileName.length() - ".svg".length()); // get rid of .svg
            fileList.add(fileName);
        }
    }


    private void buttonActions(Stage stage) {
        loadSvgFileButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Loading a skeleton path ....");
                svgFile = new SvgFileProcessor(file);
                try {
                    /** Process the svg file */
                    try {
                        svgFile.processSvg();
                        SvgFileProcessor.outputPat(svgFile.getCommandList(), svgFile.getfFileName());
                    } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        loadRegionButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Loading a region ....");
                regionFile = new SvgFileProcessor(file);
                info.setRegionFile(regionFile);
                try {
                    /** Process the svg file */
                    try {
                        regionFile.processSvg();
                        regionFile.outputSvgCommands(regionFile.getCommandList(),
                                "region-" + regionFile.getfFileName(), info);
                    } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        loadDecoElementButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Loading a pattern....");
                decoElementFile = new SvgFileProcessor(file);
                try {
                    /** Process the svg file */
                    try {
                        decoElementFile.processSvg();
                        SvgFileProcessor.outputSvgCommands(decoElementFile.getCommandList(),
                                "decoElem-" + decoElementFile.getfFileName(), info);
                    } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        generateButton.setOnAction((ActionEvent e) -> {
            /* Set Boundary */
            Region boundary = regionFile.getBoundary();

            /* Pattern Selection */
            String decoFileName = "noDeco";
            List<SvgPathCommand> decoCommands = new ArrayList<>();
            switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                case "none":
                    break;
                case "from file":
                    decoCommands = decoElementFile.getCommandList();
                    decoFileName = decoElementFile.getfFileName();
                    break;
                case "from library":
                    decoFileName = "lib" + patternLibraryComboBox.getValue().toString();
                    decoCommands = decoElementFile.getCommandList();
                    break;
            }
            /* Pattern rendering */
            SvgFileProcessor renderedDecoElemFileProcessor = null;
            List<SvgPathCommand> renderedDecoCommands = new ArrayList<>();
            int repetitions;
            String patternRenderMethod = patternRenderComboBox.getValue().toString();
            switch (patternRenderMethod) {
                case "No Rendering":
                    renderedDecoCommands = decoCommands;
                    renderedDecoElemFileProcessor = decoElementFile;
                    decoFileName += "_noRender";
                    break;
                case "Repeat with Rotation":
                case "Echo":
                    patternRenderer = new PatternRenderer(decoFileName, decoCommands, PatternRenderer.RenderType.ROTATION);
                    repetitions = Integer.valueOf(patternRenderTextFiled.getText());
                    if (patternRenderMethod.equals("Echo")) {
                        patternRenderer.echoPattern(repetitions);
                    } else {
                        patternRenderer.repeatWithRotation(repetitions);
                    }
                    renderedDecoCommands = patternRenderer.getRenderedCommands();
                    renderedDecoElemFileProcessor = new SvgFileProcessor(patternRenderer.outputRotated(Integer.valueOf(patternRenderTextFiled.getText())));
                    decoFileName += String.format("_%s_", patternRenderMethod) + repetitions;
                    break;
            }
            if (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText() != "none")
                try {
                    renderedDecoElemFileProcessor.processSvg();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            info.setDecoElementFile(renderedDecoElemFileProcessor);

            /* Skeleton Path Generation */
            PointDistribution distribute = null;
            List<SvgPathCommand> skeletonPathCommands = new ArrayList<>();
            File skeletonPathFile = null;
            SvgFileProcessor skeletonPathFileProcessor = null;
            String skeletonName = regionFile.getfFileName();
            int rows = -1;
            ArrayList<SvgPathCommand> temp = new ArrayList<>();

            skeletonName += skeletonGenComboBox.getValue().toString();
            SkeletonPathType skeletonPathType = (SkeletonPathType) skeletonGenComboBox.getValue();
            if (skeletonPathType.isTessellation()) {
                info.setPointDistributionDist(Integer.valueOf(skeletonGenTextField.getText()));
                System.out.println("Skeleton Path: Grid Tessellation");
                distribute = new PointDistribution(skeletonPathType.getPointDistributionType(), info);
                distribute.generate();
                distribute.outputDistribution();
                skeletonPathCommands = distribute.toTraversal();
                temp.addAll(skeletonPathCommands);
            } else switch (skeletonPathType) {
                case POISSON_DISK:
                    info.setSpanningTree(PointDistribution.toMST(PointDistribution.poissonDiskSamples(info)));
                    TreeTraversal renderer = new TreeTraversal(info.getSpanningTree());
                    renderer.traverseTree();
                    skeletonPathCommands = renderer.getRenderedCommands();
                    SvgFileProcessor.outputSvgCommands(skeletonPathCommands, "testNewMethod", info);
                    temp.addAll(skeletonPathCommands);

                    break;

                case HILBERT_CURVE:
                    System.out.println("Skeleton Path: Hilbert Curve...");

                    HilbertCurveGenerator hilbertcurve = new HilbertCurveGenerator(regionFile.getMinPoint(),
                            new Point(regionFile.getMaxPoint().x, 0),
                            new Point(0, regionFile.getMaxPoint().y), Integer.valueOf(skeletonGenTextField.getText()));
                    skeletonPathCommands = hilbertcurve.patternGeneration();
                    List<SvgPathCommand> fittedPath = boundary.fitCommandsToRegionTrimToBoundary(skeletonPathCommands);
                    skeletonPathCommands = fittedPath;
                    break;

                case ECHO:
                    System.out.println("Skeleton Path: Echo...");
                    PatternRenderer traversaler = new PatternRenderer(regionFile.getCommandList(), PatternRenderer.RenderType.ECHO);
                    skeletonPathCommands = traversaler.echoPattern(Integer.valueOf(skeletonGenTextField.getText()));
                    break;

                case MEDIAL_AXIS:
                    System.out.println("Skeleton Path: Medial Axis...");
                    skeletonPathCommands = boundary.generateMedialAxis();
                    break;

                case SNAKE:
                    System.out.println("Skeleton Path: Snake...");
                    SkeletonPathGenerator generator = new SkeletonPathGenerator(boundary);
                    rows = Integer.valueOf(skeletonGenTextField.getText());
                    generator.snakePathGenerator(rows);
                    skeletonPathCommands = generator.getSkeletonPath();
                    break;
            }
            skeletonPathFile = SvgFileProcessor.outputSvgCommands(skeletonPathCommands, skeletonName, info);

            if (skeletonPathFile != null) {
                skeletonPathFileProcessor = new SvgFileProcessor(skeletonPathFile);
                try {
                    skeletonPathFileProcessor.processSvg();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            /* Skeleton Path Rendering */
            PatternRenderer skeletonrenderer = null;

            /* Tree Structure based rendering */
            skeletonName += skeletonRenderComboBox.getValue().toString();
            SkeletonRenderType skeletonRenderType = (SkeletonRenderType) skeletonRenderComboBox.getValue();
            if (skeletonPathType.isTreeStructure()) {
                switch (skeletonRenderType) {
                    case CURVE:
                        skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                renderedDecoCommands, PatternRenderer.RenderType.CATMULL_ROM);
                        skeletonrenderer.toCatmullRom();

//                        /*TODO: rewrite code! below code is exactly the same as FIXED WIDTH FILL*/
//                        switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
//                            case "none":
//                                skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION);
//                                skeletonrenderer.fixedWidthFilling(0.0, Double.valueOf(skeletonRenderTextField.getText()));
//                                break;
//                            default:
//                                    /* scale deco to full*/
//                                renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
//                                        (info.getPointDistributionDist()) / (1.4 * Double.max(decoElementFile.getHeight(), decoElementFile.getWidth())),
//                                        renderedDecoCommands.get(0).getDestinationPoint());
//                                skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
//                                        renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION);
//                                skeletonrenderer.fixedWidthFilling(0.0, Double.valueOf(skeletonRenderTextField.getText()));
//                                break;
//                        }
                        SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName, info);
                        break;
                    case FIXED_WIDTH_FILL:
                        double width = info.getPointDistributionDist() / 5.0;
                        switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                            case "none":
                                skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION);
                                skeletonrenderer.fixedWidthFilling(width, Double.valueOf(skeletonRenderTextField.getText()));
                                break;
                            default:
                                    /* scale deco to full*/
                                renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                        (info.getPointDistributionDist() - width) / (1.4 * Double.max(decoElementFile.getHeight(), decoElementFile.getWidth())),
                                        renderedDecoCommands.get(0).getDestinationPoint());
                                skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                        renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION);
                                skeletonrenderer.fixedWidthFilling(width, Double.valueOf(skeletonRenderTextField.getText()));
                                break;
                        }
                        SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName, info);
                        break;
                    case PEBBLE:
                        if (info.getSpanningTree() == null) {
                        } else {
                            skeletonrenderer = new PebbleRenderer(renderedDecoCommands, info, skeletonGenComboBox.getValue().equals(VINE));
                            skeletonrenderer.pebbleFilling();
                            SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName, info);
                            temp.addAll(skeletonrenderer.getRenderedCommands());
                            SvgFileProcessor.outputSvgCommands(temp, skeletonName + "_temp_" + decoFileName, info);
                        }
                        break;
                    case RECTANGLE:
                        if (info.getSpanningTree() == null) {
                        } else {
                            skeletonrenderer = new PebbleRenderer(renderedDecoCommands, info, false);
                            ((PebbleRenderer) skeletonrenderer).rectanglePacking();
                            SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName, info);
                            temp.addAll(skeletonrenderer.getRenderedCommands());
                            SvgFileProcessor.outputSvgCommands(temp, skeletonName + "_temp_" + decoFileName, info);
                        }
                        break;
                    case NONE:
                        if (skeletonPathCommands.size() != 0) {
                            System.out.println("Skeleton Size" + skeletonPathCommands.size());
                            switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                                case "none":
                                    skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION);
                                    skeletonrenderer.fixedWidthFilling(0, Double.valueOf(skeletonRenderTextField.getText()));
                                    break;
                                default:
                                    /* scale deco to full*/
                                    renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                            info.getPointDistributionDist() / (1.4 * Double.max(decoElementFile.getHeight(), decoElementFile.getWidth())),
                                            renderedDecoCommands.get(0).getDestinationPoint());
                                    skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                            renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION);
                                    skeletonrenderer.fixedWidthFilling(0, Double.valueOf(skeletonRenderTextField.getText()));
                                    break;
                            }
                            SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName, info);
                        } else {
                            System.out.println("ERROR: skeleton path commands");
                        }
                        break;
                }
            } else {
                List<SvgPathCommand> renderedCommands = skeletonPathCommands;
                switch ((SkeletonRenderType) skeletonRenderComboBox.getValue()) {
                    case FIXED_WIDTH_FILL:
                        switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                            case "none":
                                skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION);
                                break;
                            case "from file":
                            case "from library":
                                skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                        renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION);
                                break;
                        }
                        renderedCommands = skeletonrenderer.fixedWidthFilling(5, Double.valueOf(skeletonRenderTextField.getText()));
                        break;

                    case ALONG_PATH:
                        mergedPattern = new SpinePatternMerger(skeletonName, skeletonPathCommands, renderedDecoElemFileProcessor, true);
                        /** Combine pattern */
                        mergedPattern.combinePattern();
                        renderedCommands = boundary.fitCommandsToRegionTrimToBoundary(mergedPattern.getCombinedCommands());
                        break;
                    case TILING:
                        double patternHeight = skeletonPathFileProcessor.getHeight() / rows;
                        mergedPattern = new SpinePatternMerger(skeletonName, skeletonPathCommands, renderedDecoElemFileProcessor, true);
                        /** Combine pattern */
                        mergedPattern.tilePattern(patternHeight);
                        renderedCommands = boundary.fitCommandsToRegionIntelligent(mergedPattern.getCombinedCommands());
                        break;
                }
                SvgFileProcessor.outputSvgCommands(renderedCommands, skeletonName + "_" + decoFileName, info);
            }

        });
    }

    private void setUpFont() {
        Label[] functionLabels = {textFieldLabel, regionSelectionLabel, skeletonGnerationlabel, skeletonRenderinglabel,
                patternLabel, svgToPatLabel, skeletonGenFieldLabel, skeletonRenderFieldLabel,
                patternSelectionLabel, patternRenderFieldLabel, patternRenderFieldLabel, patternRenderLabel};
        Label[] columnLabels = {regionLabel, skeletonLabel, patternLabel, toolLabel};
        Button[] functionButtons = {loadRegionButton};
        ToggleButton[] toggleButtons = {patternFromFile, noPattern, patternFromLibrary};
        for (Label label : functionLabels) {
            label.setFont(functionLabelFont);
            label.setTextFill(labelColor);
        }

        for (Label label : columnLabels) {
            label.setFont(columnLabelFont);
            label.setTextFill(columnLabelColor);
        }

        for (Button button : functionButtons)
            button.setFont(buttonFont);

        for (ToggleButton button : toggleButtons)
            button.setFont(buttonFont);

        generateButton.setFont(columnLabelFont);
        quiltingPatternGeneration.setFont(titleFont);
        quiltingPatternGeneration.setTextFill(Color.ALICEBLUE);
        generateButton.setTextFill(Color.DARKBLUE);

    }

    private void setupListeners() {
        //Selection Listeners
        /* Pattern Source Listener */
        patternLibraryComboBox.getItems().addAll("feather");
        patternSourceGroup.selectedToggleProperty().addListener((ov, toggle, new_toggle) -> {
            if (new_toggle == null) {
                fileSourceBox.getChildren().setAll();
            } else {
                if (patternSourceGroup.getSelectedToggle() == patternFromFile) {
                    System.out.println("New Pattern Source: Pattern From File ");
                    fileSourceBox.getChildren().setAll(loadDecoElementButton);
                }

                if (patternSourceGroup.getSelectedToggle() == patternFromLibrary) {
                    System.out.println("New Pattern Source: Pattern From Library ");
                    fileSourceBox.getChildren().setAll(patternLibraryComboBox);
                    SkeletonPathType skeletonPathType = (SkeletonPathType) skeletonGenComboBox.getValue();
                    if (skeletonPathType.isTreeStructure()) {
                        patternLibraryComboBox.getItems().setAll(endpointList);
                    } else {
                        if (skeletonRenderComboBox.getValue().equals(TILING))
                            patternLibraryComboBox.getItems().setAll(tileList);
                        else
                            patternLibraryComboBox.getItems().setAll(alongPathList);
                    }

                    System.out.println(patternLibraryComboBox.getItems().toString());
                }

                if (patternSourceGroup.getSelectedToggle() == noPattern) {
                    System.out.println("New Pattern Source: No Pattern");
                    fileSourceBox.getChildren().setAll();
                }
            }
        });

        /* Skeleton Generation Listener */
        skeletonGenComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(patternLibraryComboBox.getItems().toString());
            SkeletonPathType newSkeletonPathType = (SkeletonPathType) skeletonGenComboBox.getValue();
            System.out.println("Skeleton generation method changed: " + newSkeletonPathType);
            skeletonRenderComboBox.setValue(NONE);
            if (newSkeletonPathType.isTreeStructure()) {
                patternLibraryComboBox.getItems().setAll(endpointList);
                skeletonRenderComboBox.getItems().setAll(NONE, FIXED_WIDTH_FILL, PEBBLE, RECTANGLE, CURVE);
            } else if (newSkeletonPathType.equals(SNAKE)) {
                System.out.println("Snake");
                skeletonRenderComboBox.getItems().setAll(NONE, ALONG_PATH, TILING);
            } else {
                System.out.println("case 2: none tree structure");
                skeletonRenderComboBox.getItems().setAll(NONE, ALONG_PATH);
            }

            switch (newSkeletonPathType) {
                case ECHO:
                case HILBERT_CURVE:
                case SNAKE:
                case THREE_3_4_3_4_TESSELLATION:
                case GRID_TESSELLATION:
                case POISSON_DISK:
                case VINE:
                    if (newSkeletonPathType.isTreeStructure()) {
                        skeletonGenFieldLabel.setText("Point Distribution Distance:");

                    } else switch (newSkeletonPathType) {
                        case ECHO:
                            skeletonGenFieldLabel.setText("Repetitions:");
                            break;
                        case HILBERT_CURVE:
                            skeletonGenFieldLabel.setText("Level:");
                            break;
                        case SNAKE:
                            skeletonGenFieldLabel.setText("Rows:");
                            break;
                        default:
                            skeletonGenFieldLabel.setText("Unable to identify skeleton generation method");
                    }
                    skeletonGenPropertyInput.getChildren().setAll(skeletonGenFieldLabel, skeletonGenTextField);
                    break;
                default:
                    skeletonGenPropertyInput.getChildren().removeAll(skeletonGenFieldLabel, skeletonGenTextField);
                    break;
            }

            /* Tree structured */
            if (newSkeletonPathType.isTreeStructure()) {
                skeletonRenderPropertyInput.getChildren().setAll(skeletonRenderFieldLabel, skeletonRenderTextField);
            } else {
                skeletonRenderPropertyInput.getChildren().removeAll(skeletonRenderFieldLabel, skeletonRenderTextField);
            }
        });

        /* Skeleton Rendering Listener */
        skeletonRenderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            SkeletonRenderType newSelected = (SkeletonRenderType) skeletonRenderComboBox.getValue();
            System.out.println("Skeleton rendering method changed: " + newSelected);
            if (newSelected.equals(PEBBLE) || newSelected.equals(RECTANGLE)) {
                patternSourceGroup.selectToggle(noPattern);
            } else {
                patternSourceGroup.selectToggle(patternFromFile);
            }

            if (newSelected.equals(TILING)) {
                patternLibraryComboBox.getItems().setAll(tileList);
            }
        });

        /* Library ComboBox Listener/ pattern library combobox Listener */
        patternLibraryComboBox.valueProperty().addListener(((observable, oldValue, newValue) -> {
            System.out.println(patternLibraryComboBox.getItems().toString());
            String newPatternFile = patternLibraryComboBox.getValue().toString();
            File library = alongPathLibrary;
            if (skeletonRenderComboBox.getValue().equals(TILING))
                library = tileLibrary;
            /* Tree Structured */
            SkeletonPathType skeletonPathType = (SkeletonPathType) skeletonGenComboBox.getValue();
            if (skeletonPathType.isTreeStructure())
                library = endpointLibrary;
            File file = new File(library.getPath() + "/" + newPatternFile + ".svg");
            System.out.println("Loading a pattern....");
            decoElementFile = new SvgFileProcessor(file);
            try {
                decoElementFile.processSvg();
                SvgFileProcessor.outputSvgCommands(decoElementFile.getCommandList(),
                        "decoElem-" + decoElementFile.getfFileName(), info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        /* Pattern rendering Listener */
        patternRenderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            String newSelected = patternRenderComboBox.getValue().toString();
            if (newSelected.equals("No Rendering")) {
                patternPropertyInput.getChildren().removeAll(patternRenderFieldLabel, patternRenderTextFiled);
            } else {
                patternPropertyInput.getChildren().setAll(patternRenderFieldLabel, patternRenderTextFiled);
            }
        });
    }

    public enum SkeletonPathType {
        GRID_TESSELLATION, TRIANGLE_TESSELLATION, THREE_3_4_3_4_TESSELLATION, POISSON_DISK, HILBERT_CURVE, ECHO, MEDIAL_AXIS, SNAKE, VINE;

        public boolean isTreeStructure() {
            switch (this) {
                case THREE_3_4_3_4_TESSELLATION:
                case GRID_TESSELLATION:
                case POISSON_DISK:
                case VINE:
                case TRIANGLE_TESSELLATION:
                    return true;
                default:
                    return false;
            }
        }

        public boolean isTessellation() {
            switch (this) {
                case THREE_3_4_3_4_TESSELLATION:
                case GRID_TESSELLATION:
                case TRIANGLE_TESSELLATION:
                    return true;
                default:
                    return false;
            }
        }

        public PointDistribution.RenderType getPointDistributionType() {
            switch (this) {
                case GRID_TESSELLATION:
                    return PointDistribution.RenderType.GRID;
                case VINE:
                    return PointDistribution.RenderType.VINE;
                case TRIANGLE_TESSELLATION:
                    return PointDistribution.RenderType.TRIANGLE;
                default:
                    return PointDistribution.RenderType.THREE_THREE_FOUR_THREE_FOUR;
            }
        }
    }

    public enum SkeletonRenderType {
        NONE, FIXED_WIDTH_FILL, PEBBLE, TILING, ALONG_PATH, RECTANGLE, CURVE
    }

    public enum FileSourceType {
        FILE, LIBRARY, NONE;
    }


}
