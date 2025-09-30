package inventorysystem.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

public class BarcodeGenerator {

    public static Image generateBarcodeImage(String barcodeText, int width, int height) {
        try {
            Code128Bean barcodeBean = new Code128Bean();
            barcodeBean.setModuleWidth(0.2);
            barcodeBean.setHeight(height);
            barcodeBean.doQuietZone(true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                    out, "image/png", 300, BufferedImage.TYPE_BYTE_BINARY, false, 0);

            barcodeBean.generateBarcode(canvas, barcodeText);
            canvas.finish();

            BufferedImage bufferedImage = javax.imageio.ImageIO.read(
                    new java.io.ByteArrayInputStream(out.toByteArray()));

            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
