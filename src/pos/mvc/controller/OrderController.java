/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pos.mvc.controller;

import com.mysql.cj.xdevapi.PreparableStatement;
import com.sun.jdi.connect.spi.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import pos.mvc.db.DBConnection;
import pos.mvc.model.OrderDetailModel;
import pos.mvc.model.OrderModel;

/**
 *
 * @author DELL i5
 */
public class OrderController {

    public String placeOrder(OrderModel orderModel, ArrayList<OrderDetailModel> orderDetailModels) throws SQLException {
        java.sql.Connection connection = DBConnection.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);
            String orderQuary = "INSERT INTO orders VALUES(?,?,?)";

            PreparedStatement statementForOrders = connection.prepareStatement(orderQuary);
            statementForOrders.setString(1, orderModel.getOrderID());
            statementForOrders.setString(2, orderModel.getOrderDate());
            statementForOrders.setString(3, orderModel.getCustomerId());

            if (statementForOrders.executeUpdate() > 0) {

                boolean isOrderDetailSaved = true;
                String orderDetailQuary = "INSERT INTO orderdetail VALUES(?,?,?,?)";
                for (OrderDetailModel orderDetailModel : orderDetailModels) {
                    PreparedStatement statementForOrderDetails = connection.prepareStatement(orderDetailQuary);
                    statementForOrderDetails.setString(1, orderModel.getOrderID());
                    statementForOrderDetails.setString(2, orderDetailModel.getItemCode());
                    statementForOrderDetails.setInt(3, orderDetailModel.getQty());
                    statementForOrderDetails.setDouble(4, orderDetailModel.getDiscount());

                    if (!(statementForOrderDetails.executeUpdate() > 0)) {
                        isOrderDetailSaved = false;
                    }
                }

                if (isOrderDetailSaved) {

                    boolean isItemUpdated = true;
                    String itemQuary = "UPDATE item SET QtyOnHand = QtyOnHand - ? WHERE ItemCode = ?";
                    for (OrderDetailModel orderDetailModel : orderDetailModels) {
                        PreparedStatement statementForItems = connection.prepareStatement(itemQuary);
                        statementForItems.setInt(1, orderDetailModel.getQty());
                        statementForItems.setString(2, orderDetailModel.getItemCode());
                        if (!(statementForItems.executeUpdate() > 0)) {
                            isItemUpdated = false;
                        }
                    }

                    if (isItemUpdated) {
                        connection.commit();
                        return "Success";
                    } else {
                        connection.rollback();
                        return "Item Update Error";
                    }

                } else {
                    connection.rollback();
                    return "Order Detail Save Error";
                }

            } else {
                connection.rollback();
                return "Order Save Error";
            }

        } catch (Exception e) {
            connection.rollback();
            e.printStackTrace();
            return e.getMessage();
        } finally {
            connection.setAutoCommit(true);
        }

    }

    public boolean checkDiscount(String discount) {
        try {
            int number = Integer.parseInt(discount);
            return true;
        } catch (NumberFormatException e1) {
            try {
                double number = Double.parseDouble(discount);
                return true;
            } catch (NumberFormatException e2) {
                return false;
            }
        }
    }

    public boolean checkQTY(String qty) {
        try {
            int number = Integer.parseInt(qty);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
