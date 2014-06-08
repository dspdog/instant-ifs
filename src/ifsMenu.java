import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ifsMenu implements ItemListener{

    ifsys myIfsSys;

    CheckboxMenuItem XYButton;
    CheckboxMenuItem XZButton;
    CheckboxMenuItem YZButton;

    public ifsMenu(Frame f, ifsys is){

        myIfsSys = is;

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

            XYButton.setState(is.theVolume.preferredDirection == ifsys.ViewDirection.XY);
            XZButton.setState(is.theVolume.preferredDirection == ifsys.ViewDirection.XZ);
            YZButton.setState(is.theVolume.preferredDirection == ifsys.ViewDirection.YZ);

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
    }

    public void itemStateChanged(ItemEvent e) {
        //RENDER MENU
            if(e.getItem()=="Anti-Aliasing"){
                myIfsSys.theVolume.antiAliasing = e.getStateChange()==1;
            }
        //VIEW MENU
            if(e.getItem()=="XY"){
               // if(e.getStateChange()==1){
                    myIfsSys.theVolume.preferredDirection = ifsys.ViewDirection.XY;
              //  }
                XYButton.setState(myIfsSys.theVolume.preferredDirection == ifsys.ViewDirection.XY);
                XZButton.setState(myIfsSys.theVolume.preferredDirection == ifsys.ViewDirection.XZ);
                YZButton.setState(myIfsSys.theVolume.preferredDirection == ifsys.ViewDirection.YZ);
            }
            if(e.getItem()=="XZ"){
               // if(e.getStateChange()==1){
                    myIfsSys.theVolume.preferredDirection = ifsys.ViewDirection.XZ;
                //}
                XYButton.setState(myIfsSys.theVolume.preferredDirection == ifsys.ViewDirection.XY);
                XZButton.setState(myIfsSys.theVolume.preferredDirection == ifsys.ViewDirection.XZ);
                YZButton.setState(myIfsSys.theVolume.preferredDirection == ifsys.ViewDirection.YZ);
            }
            if(e.getItem()=="YZ"){
               // if(e.getStateChange()==1){
                    myIfsSys.theVolume.preferredDirection = ifsys.ViewDirection.YZ;
               // }
                XYButton.setState(myIfsSys.theVolume.preferredDirection == ifsys.ViewDirection.XY);
                XZButton.setState(myIfsSys.theVolume.preferredDirection == ifsys.ViewDirection.XZ);
                YZButton.setState(myIfsSys.theVolume.preferredDirection == ifsys.ViewDirection.YZ);
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
}
