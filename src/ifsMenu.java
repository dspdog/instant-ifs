import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ifsMenu implements ItemListener, ChangeListener {

    ifsys myIfsSys;

    CheckboxMenuItem XYButton;
    CheckboxMenuItem XZButton;
    CheckboxMenuItem YZButton;

    JLabel ptLabel;
    JSpinner xSpinner;
    JSpinner ySpinner;
    JSpinner zSpinner;

    boolean inited=false;
    boolean autoChange = false;

    public ifsMenu(Frame f, ifsys is, JPanel sideMenu){

        inited=false;

        myIfsSys = is;

        //SIDE MENU

            xSpinner = new JSpinner();
            ySpinner = new JSpinner();
            zSpinner = new JSpinner();

            SpringLayout layout = new SpringLayout();
            sideMenu.setLayout(layout);

            ptLabel = new JLabel("Point -1");
            JLabel xlab = new JLabel("X");
            JLabel ylab = new JLabel("Y");
            JLabel zlab = new JLabel("Z");

            layout.putConstraint(SpringLayout.NORTH, ptLabel, 5, SpringLayout.NORTH, sideMenu);

            layout.putConstraint(SpringLayout.WEST, xlab, 5, SpringLayout.WEST, sideMenu);
            layout.putConstraint(SpringLayout.WEST, xSpinner, 15, SpringLayout.WEST, sideMenu);
            layout.putConstraint(SpringLayout.EAST, xSpinner, 100, SpringLayout.WEST, sideMenu);
            layout.putConstraint(SpringLayout.NORTH, xSpinner, 25, SpringLayout.NORTH, sideMenu);
            layout.putConstraint(SpringLayout.NORTH, xlab, 25, SpringLayout.NORTH, sideMenu);

            layout.putConstraint(SpringLayout.WEST, ylab, 5, SpringLayout.WEST, sideMenu);
            layout.putConstraint(SpringLayout.WEST, ySpinner, 15, SpringLayout.WEST, sideMenu);
            layout.putConstraint(SpringLayout.EAST, ySpinner, 100, SpringLayout.WEST, sideMenu);
            layout.putConstraint(SpringLayout.NORTH, ySpinner, 45, SpringLayout.NORTH, sideMenu);
            layout.putConstraint(SpringLayout.NORTH, ylab, 45, SpringLayout.NORTH, sideMenu);

            layout.putConstraint(SpringLayout.WEST, zlab, 5, SpringLayout.WEST, sideMenu);
            layout.putConstraint(SpringLayout.WEST, zSpinner, 15, SpringLayout.WEST, sideMenu);
            layout.putConstraint(SpringLayout.EAST, zSpinner, 100, SpringLayout.WEST, sideMenu);
            layout.putConstraint(SpringLayout.NORTH, zSpinner, 65, SpringLayout.NORTH, sideMenu);
            layout.putConstraint(SpringLayout.NORTH, zlab, 65, SpringLayout.NORTH, sideMenu);

            xSpinner.addChangeListener(this);
            ySpinner.addChangeListener(this);
            zSpinner.addChangeListener(this);

            sideMenu.add(ptLabel);
            sideMenu.add(xlab);
            sideMenu.add(xSpinner);
            sideMenu.add(ylab);
            sideMenu.add(ySpinner);
            sideMenu.add(zlab);
            sideMenu.add(zSpinner);


        MenuBar menuBar;
        Menu renderMenu, shapeMenu, guidesMenu, viewMenu;

        menuBar = new MenuBar();
        renderMenu = new Menu("Render");
        shapeMenu = new Menu("Shape");
        guidesMenu = new Menu("Guides");
        viewMenu = new Menu("View");

        //RENDER MENU
            CheckboxMenuItem aaButton = new CheckboxMenuItem("Anti-Aliasing"); //anti-aliasing toggle
            aaButton.setState(is.theVolume.antiAliasing);
            aaButton.addItemListener(this);
            renderMenu.add(aaButton);

        //VIEW MENU
            XYButton = new CheckboxMenuItem("XY");
            XYButton.addItemListener(this);
            viewMenu.add(XYButton);

            XZButton = new CheckboxMenuItem("XZ");
            XZButton.addItemListener(this);
            viewMenu.add(XZButton);

            YZButton = new CheckboxMenuItem("YZ");
            YZButton.addItemListener(this);
            viewMenu.add(YZButton);

            XYButton.setState(is.theVolume.preferredDirection == volume.ViewDirection.XY);
            XZButton.setState(is.theVolume.preferredDirection == volume.ViewDirection.XZ);
            YZButton.setState(is.theVolume.preferredDirection == volume.ViewDirection.YZ);

        //SHAPE MENU
            CheckboxMenuItem autoScaleButton = new CheckboxMenuItem("AutoScale Points"); //autoscale toggle
            autoScaleButton.setState(is.shape.autoScale);
            autoScaleButton.addItemListener(this);
            shapeMenu.add(autoScaleButton);

            CheckboxMenuItem imgButton = new CheckboxMenuItem("PDF Samples"); //img samples toggle
            imgButton.setState(is.usePDFSamples);
            imgButton.addItemListener(this);
            shapeMenu.add(imgButton);

        //GUIDES MENU
            CheckboxMenuItem infoButton = new CheckboxMenuItem("Info Box"); //info box toggle
            infoButton.setState(!is.infoHidden);
            infoButton.addItemListener(this);
            guidesMenu.add(infoButton);

            menuBar.add(renderMenu);
            menuBar.add(shapeMenu);
            menuBar.add(guidesMenu);
            menuBar.add(viewMenu);

            f.setMenuBar(menuBar);
            inited=true;
    }

    public void updateSideMenu(){
        autoChange = true;
        ptLabel.setText(" Point " + myIfsSys.pointSelected + " Properties:");
        if(inited){
            if(myIfsSys.pointSelected!=-1){
                xSpinner.setValue(myIfsSys.selectedPt.x);
                ySpinner.setValue(myIfsSys.selectedPt.y);
                zSpinner.setValue(myIfsSys.selectedPt.z);
            }
        }
        autoChange = false;
    }

    public void itemStateChanged(ItemEvent e) {
        //RENDER MENU
            if(e.getItem()=="Anti-Aliasing"){
                myIfsSys.theVolume.antiAliasing = e.getStateChange()==1;
            }
        //VIEW MENU
            if(e.getItem()=="XY"){
               // if(e.getStateChange()==1){
                    myIfsSys.theVolume.preferredDirection = volume.ViewDirection.XY;
              //  }
                XYButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XY);
                XZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XZ);
                YZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.YZ);
            }
            if(e.getItem()=="XZ"){
               // if(e.getStateChange()==1){
                    myIfsSys.theVolume.preferredDirection = volume.ViewDirection.XZ;
                //}
                XYButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XY);
                XZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XZ);
                YZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.YZ);
            }
            if(e.getItem()=="YZ"){
               // if(e.getStateChange()==1){
                    myIfsSys.theVolume.preferredDirection = volume.ViewDirection.YZ;
               // }
                XYButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XY);
                XZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XZ);
                YZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.YZ);
            }

        //GUIDES MENU
            if(e.getItem()=="Info Box"){
                myIfsSys.infoHidden = e.getStateChange()==2;
            }
        //SHAPE MENU
            if(e.getItem()=="AutoScale Points"){
                myIfsSys.shape.autoScale = e.getStateChange()==1;
            }
            if(e.getItem()=="PDF Samples"){
                myIfsSys.usePDFSamples = e.getStateChange()==1;
            }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(!autoChange){
            myIfsSys.selectedPt.x = Double.parseDouble(xSpinner.getValue().toString());
            myIfsSys.selectedPt.y = Double.parseDouble(ySpinner.getValue().toString());
            myIfsSys.selectedPt.z = Double.parseDouble(zSpinner.getValue().toString());
            myIfsSys.shape.updateCenter();
            myIfsSys.clearframe();
        }
    }
}
