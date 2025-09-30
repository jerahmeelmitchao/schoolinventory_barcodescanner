/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventorysystem.controllers;

/**
 *
 * @author ACER ASPIRE
 */

import inventorysystem.models.Item;
import inventorysystem.utils.BarcodeGenerator;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;

public class BarcodeTableCell extends TableCell<Item, String> {

    private final ImageView imageView = new ImageView();

    @Override
    protected void updateItem(String barcode, boolean empty) {
        super.updateItem(barcode, empty);
        if (empty || barcode == null) {
            setGraphic(null);
        } else {
            imageView.setImage(BarcodeGenerator.generateBarcodeImage(barcode, 150, 50));
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(50);
            setGraphic(imageView);
        }
    }
}
