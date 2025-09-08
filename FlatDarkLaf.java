import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.ColorUIResource;

public class FlatDarkLaf extends MetalLookAndFeel {
    
    public FlatDarkLaf() {
        setCurrentTheme(new DarkTheme());
    }
    
    @Override
    public String getName() {
        return "FlatDarkLaf";
    }
    
    @Override
    public String getID() {
        return "FlatDarkLaf";
    }
    
    @Override
    public String getDescription() {
        return "A simple dark look and feel";
    }
    
    @Override
    public boolean isNativeLookAndFeel() {
        return false;
    }
    
    @Override
    public boolean isSupportedLookAndFeel() {
        return true;
    }
    
    private static class DarkTheme extends DefaultMetalTheme {
        
        @Override
        public String getName() {
            return "Dark Theme";
        }
        
        // Define colores oscuros
        private final ColorUIResource primary1 = new ColorUIResource(102, 102, 102);
        private final ColorUIResource primary2 = new ColorUIResource(153, 153, 153);
        private final ColorUIResource primary3 = new ColorUIResource(204, 204, 204);
        
        private final ColorUIResource secondary1 = new ColorUIResource(51, 51, 51);
        private final ColorUIResource secondary2 = new ColorUIResource(102, 102, 102);
        private final ColorUIResource secondary3 = new ColorUIResource(153, 153, 153);
        
        @Override
        protected ColorUIResource getPrimary1() { return primary1; }
        
        @Override
        protected ColorUIResource getPrimary2() { return primary2; }
        
        @Override
        protected ColorUIResource getPrimary3() { return primary3; }
        
        @Override
        protected ColorUIResource getSecondary1() { return secondary1; }
        
        @Override
        protected ColorUIResource getSecondary2() { return secondary2; }
        
        @Override
        protected ColorUIResource getSecondary3() { return secondary3; }
        
        @Override
        protected ColorUIResource getBlack() { return new ColorUIResource(255, 255, 255); }
        
        @Override
        protected ColorUIResource getWhite() { return new ColorUIResource(60, 60, 60); }
    }
}