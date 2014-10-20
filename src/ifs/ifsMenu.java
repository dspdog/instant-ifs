package ifs;

import com.alee.extended.colorchooser.WebGradientColorChooser;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.laf.button.WebButton;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;
import ifs.thirdparty.ImagePreviewPanel;
import ifs.thirdparty.SliderWithSpinner;
import ifs.thirdparty.SliderWithSpinnerModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

final class ifsMenu extends Component implements ItemListener, ChangeListener, ActionListener, MouseMotionListener {

    ifsys myIfsSys;

    JLabel ptLabel;
    JLabel generationLabel;
    SliderWithSpinner xSpinner;
    SliderWithSpinner ySpinner;
    SliderWithSpinner zSpinner;

    SliderWithSpinner brightnessSpinner;
    SliderWithSpinner samplesSpinner;
    SliderWithSpinner iterationsSpinner;

    SliderWithSpinner potentialSpinner;
    SliderWithSpinner delaySpinner;
    //JSpinner dotSizeSpinner;


    SliderWithSpinner pitchSpinner;
    SliderWithSpinner yawSpinner;
    SliderWithSpinner rollSpinner;

    SliderWithSpinner camScaleSpinner;
    SliderWithSpinner camJitterSpinner;

    SliderWithSpinner evolveIntensitySpinner;
    SliderWithSpinner evolveSpeedSpinner;
    SliderWithSpinner evolveLockSpinner;
    SliderWithSpinner randomSeedSpinner;
    SliderWithSpinner randomScaleSpinner;
    SliderWithSpinner pruneThreshSpinner;

    SliderWithSpinner smearWobbleSpinner;
    SliderWithSpinner smearSizeSpinner;
    SliderWithSpinner smearSmoothSpinner;

    SliderWithSpinner scaleSpinner;

    //JCheckBox gridCheck;
    //JCheckBox cartoonCheck;

    JCheckBox perspectiveCheck;

    JCheckBox smearCheck;

    JComboBox renderModeCombo;
    JComboBox evolveModeCombo;
    JComboBox pdfModeCombo;

    WebTable evolutionTable;

    WebGradientColorChooser colorChooser;

    WebButton LockButton;
    WebButton PlayButton;

    JPanel pointProperties = new JPanel();
    JPanel renderProperties = new JPanel();
    JPanel pdfProperties = new JPanel();

    JPanel shapeProperties = new JPanel();

    Frame parentFrame = new Frame();

    int pdfXImgFile = 0;
    int pdfYImgFile = 0;
    int pdfZImgFile = 0;

    JFileChooser fc;

    boolean inited=false;
    boolean autoChange = false;
    long lastUiChange = 0;

    long lastPdfPropertiesMouseMoved=0;

    final ChangeListener updateNoClear = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            if(!autoChange){

                ifsPt editPt = new ifsPt();
                if(myIfsSys.evolutionDescSelected){
                    editPt = myIfsSys.mutationDescriptorPt;
                }else{
                    editPt = myIfsSys.theShape.selectedPt;
                }

                lastUiChange=System.currentTimeMillis();
                editPt.x = (float)Double.parseDouble(xSpinner.getValue()+"");
                editPt.y = (float)Double.parseDouble(ySpinner.getValue()+"");
                editPt.z = (float)Double.parseDouble(zSpinner.getValue()+"");
                editPt.scale = (float)(0.01 * Double.parseDouble(scaleSpinner.getValue()+""));

                editPt.rotationPitch = (float)(Double.parseDouble(pitchSpinner.getValue()+"")/180.0*Math.PI);
                editPt.rotationYaw = (float)(Double.parseDouble(yawSpinner.getValue()+"")/180.0*Math.PI);
                editPt.rotationRoll = (float)(Double.parseDouble(rollSpinner.getValue()+"")/180.0*Math.PI);

                myIfsSys.rp.iterations = Integer.parseInt(iterationsSpinner.getValue() + "");
                //myIfsSys.rp.dotSize = Integer.parseInt(dotSizeSpinner.getValue().toString());
                myIfsSys.rp.brightnessMultiplier = Double.parseDouble(brightnessSpinner.getValue()+"");
                myIfsSys.rp.samplesPerFrame = (int)Double.parseDouble(samplesSpinner.getValue()+"");

                myIfsSys.theVolume.usePerspective = !perspectiveCheck.isSelected();
                myIfsSys.rp.smearPDF = smearCheck.isSelected();
                myIfsSys.rp.shutterPeriod = delaySpinner.getValue();

                myIfsSys.rp.potentialRadius = Integer.parseInt(potentialSpinner.getValue()+"");
                //myIfsSys.rp.drawGrid = gridCheck.isSelected();
                //myIfsSys.rp.cartoonMode = cartoonCheck.isSelected();

                myIfsSys.theShape.updateCenter();


                myIfsSys.rp.jitter = camJitterSpinner.getValue();
                myIfsSys.rp.perspectiveScale = camScaleSpinner.getValue();
                myIfsSys.rp.perspectiveScale = Math.max(0.1f, myIfsSys.rp.perspectiveScale);

                myIfsSys.lastMoveTime = System.currentTimeMillis();

                myIfsSys.rp.evolveIntensity = evolveIntensitySpinner.getValue();
                myIfsSys.rp.evolveAnimationPeriod = evolveSpeedSpinner.getValue();
                myIfsSys.rp.evolveLockPeriod = evolveLockSpinner.getValue();
                myIfsSys.rp.randomSeed = randomSeedSpinner.getValue();
                myIfsSys.rp.randomScale = randomScaleSpinner.getValue();
                myIfsSys.rp.pruneThresh = pruneThreshSpinner.getValue();

                myIfsSys.rp.smearSmooth = smearSmoothSpinner.getValue();
                myIfsSys.rp.smearWobbleIntensity = smearWobbleSpinner.getValue();
                myIfsSys.rp.smearSize = smearSizeSpinner.getValue();

                myIfsSys.rp.odbScale.setIntensity((float)Math.sqrt(myIfsSys.rp.smearWobbleIntensity)/10f);
                myIfsSys.rp.odbRotationRoll.setIntensity((float)Math.sqrt(myIfsSys.rp.smearWobbleIntensity)/10f);
                myIfsSys.rp.odbX.setIntensity((float)Math.sqrt(myIfsSys.rp.smearWobbleIntensity)/10f);
                myIfsSys.rp.odbY.setIntensity((float)Math.sqrt(myIfsSys.rp.smearWobbleIntensity)/10f);
                myIfsSys.rp.odbZ.setIntensity((float)Math.sqrt(myIfsSys.rp.smearWobbleIntensity)/10f);

                myIfsSys.theVolume.changed=true;
            }
        }
    };

    ChangeListener updateAndClear = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            updateNoClear.stateChanged(e);
            myIfsSys.clearframe();
        }
    };

    public JButton addLabeledButton(WebButton theButton, SpringLayout layout, JPanel panel, int col){
        int width = 51;

        int spinnerLeft = 5 + col*width;
        int spinnerRight = 5 + col*width+ width;

        int topPad=25;

        layout.putConstraint(SpringLayout.WEST, theButton, spinnerLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, theButton, spinnerRight, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, theButton, (int)(topPad+panel.getHeight()), SpringLayout.NORTH, panel);

        //button.addActionListener(this);

        panel.add(theButton);

        return theButton;
    }
    public <T extends JComponent> JComponent addLabeled(T comp, SpringLayout layout, String labelText, JPanel panel){
        return addLabeled(comp, layout, labelText, panel, false);
    }

    public <T extends JComponent> JComponent addLabeled(T comp, SpringLayout layout, String labelText, JPanel panel, boolean noLabel){
        int compLeft = 70;
        int compRight = -5;
        int compToSpinner = -5;
        int topPad=28;

        if(noLabel){ //things without labels get centered...
            compLeft=30;
        }

        JLabel label = new JLabel(labelText);

        layout.putConstraint(SpringLayout.EAST, label, compToSpinner, SpringLayout.WEST, comp);
        layout.putConstraint(SpringLayout.WEST, comp, compLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, comp, compRight, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, comp, (int)(panel.getHeight()+0), SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, label, (int) (panel.getHeight() + 0), SpringLayout.NORTH, panel);

        if(!noLabel)panel.add(label);
        panel.add(comp);panel.setSize(200,topPad+panel.getHeight());

        return comp;
    }

    public void setupPointPropertiesPanel(JPanel panel){
        xSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 1024));
        ySpinner = new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 1024));
        zSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 1024));
        scaleSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 1000));

        pitchSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 360));
        yawSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 360));
        rollSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 360));

        ptLabel = new JLabel("Point -1");

        int topPad=5;

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);
        //panel.add(ptLabel);
        //addLabel(ptLabel, layout, panel);
        ((JLabel)addLabeled(ptLabel, layout, "Point", panel)).addComponentListener(null);
        ((SliderWithSpinner)addLabeled(xSpinner, layout, "X", panel)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(ySpinner, layout, "Y", panel)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(zSpinner, layout, "Z", panel)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(scaleSpinner, layout, "Scale %", panel)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(pitchSpinner, layout, "Pitch°", panel)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(yawSpinner, layout, "Yaw°", panel)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(rollSpinner, layout, "Roll°", panel)).addChangeListener(updateAndClear);
    }

    public void setupPdfPropertiesPanel(JPanel panel){
        //JLabel renderLabel = new JLabel(" PDF Properties");

        smearCheck = new JCheckBox();

        int topPad=5;

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        smearWobbleSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(5, 0, 200));
        smearSmoothSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(512, 0, 2000));
        smearSizeSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(16, 0, 200));

        WebButton XButton = new WebButton("", new ImageIcon("./instant-ifs/icons/front.png"));
        WebButton ZButton = new WebButton("", new ImageIcon("./instant-ifs/icons/side.png"));
        WebButton YButton = new WebButton("", new ImageIcon("./instant-ifs/icons/top.png"));

        XButton.setName("X");XButton.addActionListener(this);
        YButton.setName("Y");YButton.addActionListener(this);
        ZButton.setName("Z");ZButton.addActionListener(this);

        WebButtonGroup iconsGroup = new WebButtonGroup ( true, XButton, YButton, ZButton );
        //((WebButtonGroup)addLabeled(iconsGroup, layout, "", panel, true)).addComponentListener(null);
        //((JLabel)addLabeled(new JLabel(""), layout, "", panel, true)).addComponentListener(null);
        ((JCheckBox)addLabeled(smearCheck, layout, "Smear", panel)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(smearWobbleSpinner, layout, "WobbleMag", panel, false)).addChangeListener(updateAndClear);
        //((SliderWithSpinner)addLabeled(smearSmoothSpinner, layout, "WobSmooth", panel, false)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(smearSizeSpinner, layout, "Size", panel, false)).addChangeListener(updateAndClear);

        panel.addMouseMotionListener(this);
    }


    public void setupShapePropertiesPanel(JPanel panel){

        int topPad=5;
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        //((JLabel)addLabeled(generationLabel, layout, "Generation", panel)).addComponentListener(null);

        WebButton ParentsButton = new WebButton("", new ImageIcon("./instant-ifs/icons/parents.png"));
        WebButton OffspingButton = new WebButton("", new ImageIcon("./instant-ifs/icons/offspring.png"));

        WebButton NextSibButton = new WebButton("", new ImageIcon("./instant-ifs/icons/next.png"));
        WebButton PrevSibButton = new WebButton("", new ImageIcon("./instant-ifs/icons/back.png"));
        LockButton = new WebButton("", new ImageIcon("./instant-ifs/icons/unlock.png"));
        PlayButton = new WebButton("", new ImageIcon("./instant-ifs/icons/invader_big.png"));

        ParentsButton.setName("parents");ParentsButton.addActionListener(this);
        OffspingButton.setName("offspring");OffspingButton.addActionListener(this);
        NextSibButton.setName("nextsib");NextSibButton.addActionListener(this);
        PrevSibButton.setName("prevsib");PrevSibButton.addActionListener(this);
        LockButton.setName("lock");LockButton.addActionListener(this);
        PlayButton.setName("play");PlayButton.addActionListener(this);



        iterationsSpinner =new SliderWithSpinner(new SliderWithSpinnerModel(3, 0, 10));
        ((SliderWithSpinner)addLabeled(iterationsSpinner, layout, "Iterations", panel)).addChangeListener(updateAndClear);

        pruneThreshSpinner=new SliderWithSpinner(new SliderWithSpinnerModel(10, 0, 100));
        ((SliderWithSpinner)addLabeled(pruneThreshSpinner, layout, "Prune", panel)).addChangeListener(updateAndClear);

        randomSeedSpinner=new SliderWithSpinner(new SliderWithSpinnerModel(1000, 0, 100_000));
        ((SliderWithSpinner)addLabeled(randomSeedSpinner, layout, "Rnd Seed", panel)).addChangeListener(updateAndClear);

        randomScaleSpinner=new SliderWithSpinner(new SliderWithSpinnerModel(10, 0, 1000));
        ((SliderWithSpinner)addLabeled(randomScaleSpinner, layout, "Rnd Scale", panel)).addChangeListener(updateAndClear);



        WebButtonGroup iconsGroup = new WebButtonGroup ( true,
                //OffspingButton, ParentsButton, PrevSibButton, NextSibButton,
                LockButton, PlayButton );
        ((WebButtonGroup)addLabeled(iconsGroup, layout, "", panel, true)).addComponentListener(null);

        ((JLabel)addLabeled(new JLabel(), layout, "", panel, true)).addComponentListener(null);

        evolveIntensitySpinner=new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 1000));
        evolveSpeedSpinner=new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 1000));
        evolveLockSpinner=new SliderWithSpinner(new SliderWithSpinnerModel(1000, 0, 20000));
/*
        final WebImage webImage1 = new WebImage (myIfsSys.imageUtils.getImage("_x.png")).setDisplayType(DisplayType.preferred);
        webImage1.setPreferredSize(new Dimension(100,64));
        TooltipManager.setTooltip(webImage1, "example image", TooltipWay.up);
        ((WebImage)addLabeled(webImage1, layout, "Img Ex", panel)).addMouseMotionListener(new MouseMotionListener() {

            float posX, posY;

            @Override
            public void mouseDragged(MouseEvent e) {
                float yScale = 5*0.001f;
                posX = e.getX();
                posY = e.getY();
                myIfsSys.rp.odbX.set(1.0f/yScale*(posY/webImage1.getHeight()-0.5f),posX/webImage1.getWidth());
                MemoryImageSource mis = new MemoryImageSource(webImage1.getWidth(), webImage1.getHeight(),
                        myIfsSys.rp.odbX.getPixels(webImage1.getWidth(), webImage1.getHeight(), 1f, yScale), 0, webImage1.getWidth());
                webImage1.setImage(myIfsSys.createImage(mis));
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                //System.out.println("moved");
            }
        });
*/
        //((JLabel)addLabeled(new JLabel(), layout, "", panel)).addComponentListener(null);
        //((JLabel)addLabeled(new JLabel(), layout, "", panel)).addComponentListener(null);
        ((SliderWithSpinner)addLabeled(evolveIntensitySpinner, layout, "Intensity", panel)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(evolveSpeedSpinner, layout, "Period", panel)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(evolveLockSpinner, layout, "Lock Period", panel)).addChangeListener(updateAndClear);
                ((JLabel)addLabeled(new JLabel(), layout, "", panel)).addComponentListener(null);

        //evolutionTable.setEditable(false);
        //evolutionTable.setAutoResizeMode(WebTable.AUTO_RESIZE_OFF);
        //evolutionTable.setRowSelectionAllowed(true);
        //evolutionTable.setColumnSelectionAllowed(true);
        //evolutionTable.setPreferredScrollableViewportSize(new Dimension(200, 150));
        //evolutionTable.setAutoCreateRowSorter(true);
        //evolutionTable.setAutoscrolls(true);
         ((WebScrollPane)addLabeled(new WebScrollPane ( evolutionTable ), layout, "", panel, true)).addComponentListener(new ComponentListener() {
             @Override
             public void componentResized(ComponentEvent e) {

             }

             @Override
             public void componentMoved(ComponentEvent e) {

             }

             @Override
             public void componentShown(ComponentEvent e) {

             }

             @Override
             public void componentHidden(ComponentEvent e) {

             }
         });

        panel.addMouseMotionListener(this);
    }

    public void setupRenderPropertiesPanel(JPanel panel){
        brightnessSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 360));
        samplesSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 2000));

        potentialSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 360));
        delaySpinner = new SliderWithSpinner(new SliderWithSpinnerModel(50, 0, 100));
        //dotSizeSpinner = new JSpinner();

        //frameHoldCheck = new JCheckBox();

        //gridCheck = new JCheckBox();
        //cartoonCheck = new JCheckBox();

        //delayCheck = new JCheckBox();

        String[] renderModeStrings = {volume.RenderMode.VOLUMETRIC.toString(), volume.RenderMode.PROJECT_ONLY.toString()};

        renderModeCombo = new JComboBox(renderModeStrings);

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        ((JComboBox)addLabeled(renderModeCombo, layout, "Mode", panel)).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                if (cb.getSelectedItem() == volume.RenderMode.VOLUMETRIC.toString()) {
                    myIfsSys.theVolume.renderMode = volume.RenderMode.VOLUMETRIC;
                } else if (cb.getSelectedItem() == volume.RenderMode.PROJECT_ONLY.toString()) {
                    myIfsSys.theVolume.renderMode = volume.RenderMode.PROJECT_ONLY;
                }
            }
        });

        ((SliderWithSpinner)addLabeled(brightnessSpinner, layout, "Brightness", panel)).addChangeListener(updateAndClear);
        //((SliderWithSpinner)addLabeled(samplesSpinner, layout, "Dots/Frame", panel)).addChangeListener(updateAndClear);

        //((JSpinner)addLabeled(potentialSpinner, layout, "Blur", panel)).addChangeListener(updateAndClear);
        //((JCheckBox)addLabeled(cartoonCheck, layout, "Cartoon", panel)).addChangeListener(updateAndClear);

        //((JCheckBox)addLabeled(gridCheck, layout, "Grid", panel)).addChangeListener(updateAndClear);
        //((JCheckBox)addLabeled(frameHoldCheck, layout, "Hold Frame", panel)).addChangeListener(updateAndClear);

        //((JCheckBox)addLabeled(delayCheck, layout, "Throttle", panel)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(delaySpinner, layout, "Shutter", panel)).addChangeListener(updateAndClear);
        //((JSpinner)addLabeled(dotSizeSpinner, layout, "Dot Size", panel)).addChangeListener(updateAndClear);

        //xMaxSpinner=new JSpinner();
        //xMinSpinner=new JSpinner();
        //yMaxSpinner=new JSpinner();
        //yMinSpinner=new JSpinner();
        //zMaxSpinner=new JSpinner();
        //zMinSpinner=new JSpinner();

        //((JSpinner)addLabeled(xMinSpinner, layout, "Xmin", panel)).addChangeListener(updateAndClear);
        //((JSpinner)addLabeled(xMaxSpinner, layout, "Xmax", panel)).addChangeListener(updateAndClear);
        //((JSpinner)addLabeled(yMinSpinner, layout, "Ymin", panel)).addChangeListener(updateAndClear);
        //((JSpinner)addLabeled(yMaxSpinner, layout, "Ymax", panel)).addChangeListener(updateAndClear);
        //((JSpinner)addLabeled(zMinSpinner, layout, "Zmin", panel)).addChangeListener(updateAndClear);
        //((JSpinner)addLabeled(zMaxSpinner, layout, "Zmax", panel)).addChangeListener(updateAndClear);

        colorChooser = new WebGradientColorChooser();
        //colorChooser.setPreferredWidth(350);
        // ((WebGradientColorChooser)addLabeled(colorChooser, layout, "Color", panel)).addChangeListener(updateAndClear);

        camScaleSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(60, 1, 300));
        camJitterSpinner = new SliderWithSpinner(new SliderWithSpinnerModel(2, 0, 32));

        ((SliderWithSpinner)addLabeled(camScaleSpinner, layout, "FOV", panel)).addChangeListener(updateAndClear);
        ((SliderWithSpinner)addLabeled(camJitterSpinner, layout, "DOF", panel)).addChangeListener(updateAndClear);

        ActionListener moveCamera = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WebButton cb = (WebButton)(e.getSource());
                System.out.println("NAME  " + cb.getName());
                if(cb.getName()=="YZ"){
                    myIfsSys.theVolume.camPitch=-90;
                    myIfsSys.theVolume.camRoll=-90;
                    myIfsSys.theVolume.camYaw=0;
                }else if(cb.getName()=="XY"){
                    //TOP VIEW
                    myIfsSys.theVolume.camPitch=0;
                    myIfsSys.theVolume.camRoll=0;
                    myIfsSys.theVolume.camYaw=0;
                }else if(cb.getName()=="XZ"){
                    //SIDE VIEW
                    myIfsSys.theVolume.camPitch=0;
                    myIfsSys.theVolume.camRoll=-90;
                    myIfsSys.theVolume.camYaw=0;
                }else if(cb.getName()=="sports"){
                    myIfsSys.rp.shutterPeriod=2;
                    myIfsSys.rp.jitter = 0;
                }else if(cb.getName()=="portrait"){
                    myIfsSys.rp.shutterPeriod=32;
                    myIfsSys.rp.jitter = 4;
                }
                myIfsSys.clearframe();
            }
        };

        perspectiveCheck = new JCheckBox();
        ((JCheckBox)addLabeled(perspectiveCheck, layout, "Ortho", panel)).addChangeListener(updateAndClear);

        WebButton YZButton = new WebButton("", new ImageIcon("./instant-ifs/icons/front.png"));
        WebButton XYButton = new WebButton("", new ImageIcon("./instant-ifs/icons/side.png"));
        WebButton XZButton = new WebButton("", new ImageIcon("./instant-ifs/icons/top.png"));

        WebButton SportsButton = new WebButton("", new ImageIcon("./instant-ifs/icons/mountains.png"));
        WebButton PortraitButton = new WebButton("", new ImageIcon("./instant-ifs/icons/robot.png"));

        YZButton.setName("YZ");YZButton.addActionListener(moveCamera);
        XYButton.setName("XZ");XYButton.addActionListener(moveCamera);
        XZButton.setName("XY");XZButton.addActionListener(moveCamera);

        SportsButton.setName("sports");SportsButton.addActionListener(moveCamera);
        PortraitButton.setName("portrait");PortraitButton.addActionListener(moveCamera);

        WebButtonGroup iconsGroup = new WebButtonGroup ( true, YZButton, XZButton, XYButton );
        ((WebButtonGroup)addLabeled(iconsGroup, layout, "", panel, true)).addComponentListener(null);

        WebButtonGroup iconsGroup2 = new WebButtonGroup ( true, SportsButton, PortraitButton );
        ((JLabel)addLabeled(new JLabel(), layout, "", panel, true)).addComponentListener(null);
        ((WebButtonGroup)addLabeled(iconsGroup2, layout, "", panel, true)).addComponentListener(null);
    }

    public ifsMenu(Frame f, ifsys is){
        is.addMouseListener(new PopClickListener());
        inited=false;
        parentFrame=f;
        myIfsSys = is;
        pointProperties = new JPanel();
        renderProperties = new JPanel();
        pdfProperties = new JPanel();
        //cameraProperties = new JPanel();
        shapeProperties = new JPanel();

        //SIDE MENU

        setupPointPropertiesPanel(pointProperties);
        setupRenderPropertiesPanel(renderProperties);
        setupPdfPropertiesPanel(pdfProperties);
        setupShapePropertiesPanel(shapeProperties);

        SpringLayout sideMenuLayout = new SpringLayout();

        MenuBar menuBar;
        Menu fileMenu, renderMenu, shapeMenu, guidesMenu, viewMenu;

        menuBar = new MenuBar();
        fileMenu = new Menu("File");
        renderMenu = new Menu("Render");
        shapeMenu = new Menu("Shape");
        guidesMenu = new Menu("Guides");

        //FILE MENU
            MenuItem saveButton = new MenuItem("Save Shape...");
            saveButton.addActionListener(this);
            fileMenu.add(saveButton);

            MenuItem loadButton = new MenuItem("Load Shape...");
            loadButton.addActionListener(this);
            fileMenu.add(loadButton);

        //RENDER MENU
            CheckboxMenuItem aaButton = new CheckboxMenuItem("Anti-Aliasing"); //anti-aliasing toggle
            aaButton.setState(is.theVolume.antiAliasing);
            aaButton.addItemListener(this);
            renderMenu.add(aaButton);

        //SHAPE MENU
            CheckboxMenuItem autoScaleButton = new CheckboxMenuItem("AutoScale Points"); //autoscale toggle
            autoScaleButton.setState(is.theShape.autoScale);
            autoScaleButton.addItemListener(this);
            shapeMenu.add(autoScaleButton);

            CheckboxMenuItem imgButton = new CheckboxMenuItem("Kernel Samples"); //img samples toggle
            imgButton.setState(is.rp.usePDFSamples);
            imgButton.addItemListener(this);
            shapeMenu.add(imgButton);

        //GUIDES MENU
            CheckboxMenuItem infoButton = new CheckboxMenuItem("Info Box"); //info box toggle
            infoButton.setState(!is.rp.infoHidden);
            infoButton.addItemListener(this);
            guidesMenu.add(infoButton);

            //menuBar.add(renderMenu);
            //menuBar.add(shapeMenu);
            //menuBar.add(guidesMenu);
            menuBar.add(fileMenu);

            f.setMenuBar(menuBar);
            inited=true;

        fc = new JFileChooser("./instant-ifs/img");
        ImagePreviewPanel preview = new ImagePreviewPanel();
        fc.setAccessory(preview);
        fc.addPropertyChangeListener(preview);
    }

    public void updateSideMenu(){
        autoChange = true;
        //generationLabel.setText(myIfsSys.eShape.familyHistory.size() + " Sibling " + myIfsSys.eShape.shapeIndex+"/"+myIfsSys.eShape.sibsPerGen);
        ptLabel.setText(myIfsSys.theShape.pointSelected+"");
        if(inited && System.currentTimeMillis()-lastUiChange>100){
            if(myIfsSys.theShape.pointSelected !=-1){
                ifsPt editPt;
                if(myIfsSys.evolutionDescSelected){
                    editPt = myIfsSys.mutationDescriptorPt;
                    ptLabel.setText("Evolution Mutation");
                }else if(myIfsSys.iterationDescSelected){
                    editPt = myIfsSys.theShape.iterationDescriptorPt;
                    ptLabel.setText("Iteration Mutation");
                }else{
                    editPt = myIfsSys.theShape.selectedPt;
                }

                xSpinner.setValue((int)editPt.x);
                ySpinner.setValue((int)editPt.y);
                zSpinner.setValue((int)editPt.z);

                scaleSpinner.setValue((int)(editPt.scale * 100));

                pitchSpinner.setValue((int)(editPt.rotationPitch/Math.PI*180));
                yawSpinner.setValue((int)(editPt.rotationYaw/Math.PI*180));
                rollSpinner.setValue((int)(editPt.rotationRoll/Math.PI*180));

                camScaleSpinner.setValue((int)myIfsSys.rp.perspectiveScale);
                camJitterSpinner.setValue((int)myIfsSys.rp.jitter);
                evolveIntensitySpinner.setValue((int)myIfsSys.rp.evolveIntensity);
                evolveSpeedSpinner.setValue((int)myIfsSys.rp.evolveAnimationPeriod);
                evolveLockSpinner.setValue((int)myIfsSys.rp.evolveLockPeriod);
                randomSeedSpinner.setValue((int)myIfsSys.rp.randomSeed);
                randomScaleSpinner.setValue((int)myIfsSys.rp.randomScale);
                pruneThreshSpinner.setValue((int)myIfsSys.rp.pruneThresh);

                smearWobbleSpinner.setValue((int) myIfsSys.rp.smearWobbleIntensity);
                smearSizeSpinner.setValue((int) myIfsSys.rp.smearSize);
                smearSmoothSpinner.setValue((int) myIfsSys.rp.smearSmooth);

                brightnessSpinner.setValue((int)myIfsSys.rp.brightnessMultiplier);
                samplesSpinner.setValue(myIfsSys.rp.samplesPerFrame);
                iterationsSpinner.setValue(myIfsSys.rp.iterations);
                potentialSpinner.setValue(myIfsSys.rp.potentialRadius);
                delaySpinner.setValue((int)myIfsSys.rp.shutterPeriod);

                perspectiveCheck.setSelected(!myIfsSys.theVolume.usePerspective);
                //frameHoldCheck.setSelected(myIfsSys.rp.holdFrame);
                //gridCheck.setSelected(myIfsSys.rp.drawGrid);
                //delayCheck.setSelected(myIfsSys.rp.renderThrottling);
                //cartoonCheck.setSelected(myIfsSys.rp.cartoonMode);

                smearCheck.setSelected(myIfsSys.rp.smearPDF);

                //System.out.println(renderModeCombo.setse);
                renderModeCombo.setSelectedIndex(myIfsSys.theVolume.renderMode == volume.RenderMode.PROJECT_ONLY ? 1 : 0);
                //dotSizeSpinner.setValue(myIfsSys.rp.dotSize);

                //xMinSpinner.setValue(myIfsSys.rp.xMin);
                //xMaxSpinner.setValue(myIfsSys.rp.xMax);
                //yMinSpinner.setValue(myIfsSys.rp.yMin);
                //yMaxSpinner.setValue(myIfsSys.rp.yMax);
                //zMinSpinner.setValue(myIfsSys.rp.zMin);
                //zMaxSpinner.setValue(myIfsSys.rp.zMax);
            }
        }
        /*
        switch (myIfsSys.thePdf.thePdfComboMode){
            case ADD:
                if(pdfModeCombo.getSelectedIndex()!=0)
                    pdfModeCombo.setSelectedIndex(0);
                break;
            case AVERAGE:
                if(pdfModeCombo.getSelectedIndex()!=1)
                    pdfModeCombo.setSelectedIndex(1);
                break;
            case MULTIPLY:
                if(pdfModeCombo.getSelectedIndex()!=2)
                    pdfModeCombo.setSelectedIndex(2);
                break;
            case MAX:
                if(pdfModeCombo.getSelectedIndex()!=3)
                    pdfModeCombo.setSelectedIndex(3);
                break;
            case MIN:
                if(pdfModeCombo.getSelectedIndex()!=4)
                    pdfModeCombo.setSelectedIndex(4);
                break;
        }*/

        autoChange = false;
    }

    public void itemStateChanged(ItemEvent e) {
        //RENDER MENU
            if(e.getItem()=="Anti-Aliasing"){
                myIfsSys.theVolume.antiAliasing = e.getStateChange()==1;
            }
        //GUIDES MENU
            if(e.getItem()=="Info Box"){
                myIfsSys.rp.infoHidden = e.getStateChange()==2;
            }
        //SHAPE MENU
            if(e.getItem()=="AutoScale Points"){
                myIfsSys.theShape.autoScale = e.getStateChange()==1;
            }
            if(e.getItem()=="Kernel Samples"){
                myIfsSys.rp.usePDFSamples = e.getStateChange()==1;
            }
    }

    @Override
    public void stateChanged(ChangeEvent e) {

    }

    public void actionPerformed(ActionEvent e) {
        JButton wb = new WebButton();
        JButton cb = new JButton();// = (JButton)e.getSource();
        MenuItem mc = new MenuItem();// = (MenuItem)e.getSource();

        try{
            cb = (JButton)e.getSource();
        }catch (Exception _e){

        }
        try{
            wb=(WebButton)e.getSource();
        }catch (Exception _e){

        }
        try{
            mc = (MenuItem)e.getSource();
            System.out.println(mc.getActionCommand());
        }catch (Exception _e){

        }
        if(wb.getName()=="lock"){
            myIfsSys.theShape.saveToFile("locked.shape");
            myIfsSys.theAnimationThread.shapeReload=!myIfsSys.theAnimationThread.shapeReload;
            if(myIfsSys.theAnimationThread.shapeReload){
                LockButton.setIcon(new ImageIcon("./instant-ifs/icons/lock.png"));
            }else{
                LockButton.setIcon(new ImageIcon("./instant-ifs/icons/unlock.png"));
            }
        }else if(wb.getName()=="play"){
            myIfsSys.saveStuff("");
            myIfsSys.rp.shapeVibrating = !myIfsSys.rp.shapeVibrating;

            if(myIfsSys.rp.shapeVibrating){
                PlayButton.setIcon(new ImageIcon("./instant-ifs/icons/invader_alive_big.png"));
            }else{
                PlayButton.setIcon(new ImageIcon("./instant-ifs/icons/invader_big.png"));
            }

        }else if(mc.getActionCommand()=="Save Shape..."){
            pdfZImgFile = fc.showSaveDialog(this);
            if(pdfZImgFile == JFileChooser.APPROVE_OPTION){
                System.out.println("saving " + fc.getSelectedFile().getName());
                myIfsSys.saveStuff(fc.getSelectedFile().getName());
            }
        }else if(mc.getActionCommand()=="Load Shape..."){
            pdfZImgFile = fc.showOpenDialog(this);
            if(pdfZImgFile == JFileChooser.APPROVE_OPTION){
                System.out.println("loading " + fc.getSelectedFile().getName());
                myIfsSys.loadStuff(fc.getSelectedFile().getName());
            }
        }

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //triggered by pdf properties panel
        lastPdfPropertiesMouseMoved=System.currentTimeMillis();
    }

    class PopClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent e){
            if (SwingUtilities.isRightMouseButton(e))
                doPop(e);
        }

        private void doPop(MouseEvent e){
            myPopUpMenu menu = new myPopUpMenu();
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    class myPopUpMenu extends JPopupMenu {
        JMenuItem anItem;
        public myPopUpMenu(){
            anItem = new JMenuItem("Menu1");
            add(anItem);
        }
    }
}
