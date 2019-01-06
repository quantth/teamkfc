package controller;

import database.DbConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import model.Invoice;
import model.Sale;
import model.Statistic;
import service.InvoiceService;
import service.StatisticService;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class StatisticController implements Initializable {

    /*statistic invoice start*/
    @FXML
    private TextField tf_invoiceSearchContent;

    @FXML
    private TableView<Invoice> invoiceTable;

    @FXML
    private TableColumn<Invoice, String> col_invoiceId;

    @FXML
    private TableColumn<Invoice, String> col_customer;

    @FXML
    private TableColumn<Invoice, String> col_creator;

    @FXML
    private TableColumn<Invoice, String> col_createdTime;

    @FXML
    private TableColumn<Invoice, Double> col_totalPrice;

    @FXML
    private TableColumn<Invoice, Double> col_payPrice;

    @FXML
    private TableColumn<Invoice, Button> col_detail;

    @FXML
    private Pagination invoicePagination;
    /*statistic invoice end*/

    /*sales report start*/

    @FXML
    private TextField tf_s_name;

    @FXML
    private DatePicker tf_s_date;

    @FXML
    private ComboBox<Sale> cb_sale;

    @FXML
    private TableView<Statistic> salesReportTable;

    @FXML
    private TableColumn<Statistic, String> col_s_id;

    @FXML
    private TableColumn<Statistic, String> col_s_name;

    @FXML
    private TableColumn<Statistic, String> col_s_creator;

    @FXML
    private TableColumn<Statistic, Integer> col_s_total;

    @FXML
    private TableColumn<Statistic, Double> col_s_revenue;

    @FXML
    private TableColumn<Statistic, String> col_s_sale;

    @FXML
    private Pagination salesReportPagination;

    @FXML
    private ChoiceBox<String> choiceBox;

    /*sales report end*/

    private static String searchContent;
    private static String searchName;
    private static String searchSale;
    private static Date searchTime;
    private static int choice;
    private static int statisticsSize;

    @FXML
    void invoiceSearchAction(MouseEvent event) {
        ObservableList<Invoice> comboList = null;
        InvoiceService invoiceService = new InvoiceService();

        String text = tf_invoiceSearchContent.getText();
        searchContent = text;
        indexAction();
    }

    @FXML
    void saleReportSearchAction(MouseEvent event) {
        String name = tf_s_name.getText();
        //String saleId = cb_sale.getValue().getId();
        LocalDate time = tf_s_date.getValue();

        searchTime = Date.valueOf(time);
        searchName = name;
        System.out.println(time);

        indexAction();
    }

    private ObservableList<Invoice> initData(int p, String text) {
        ObservableList<Invoice> invoices = null;
        InvoiceService invoiceService = new InvoiceService();

        try {
            invoices = invoiceService.getAllInvoice_p(null, text, p);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    private Node createInvoicePage(int p) {
        String text = null;
        if (searchContent != null) {
            text = searchContent;
        }
        ObservableList<Invoice> invoices = initData(p, text);
        invoiceTable.setItems(FXCollections.observableList(invoices));
        return invoiceTable;
    }

    //sale report page
    private ObservableList<Statistic> initSaleReportData(int p, String name, String saleId, Date time, int choice) {
        ObservableList<Statistic> statistics = null;
        StatisticService statisticService = new StatisticService();

        try {
            statistics = statisticService.getAllStatistic_p(name, saleId, time, choice, p);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statistics;
    }

    private Node createSaleReportPage(int p) {
        String name = null;
        String saleId = null;
        Date time = null;
        if (searchName != null) {
            name = searchName;
        }
        if (searchSale != null){
            saleId = searchSale;
        }
        if (String.valueOf(searchTime) != null){
            time = searchTime;
        }
        ObservableList<Statistic> statistics = initSaleReportData(p, name, saleId, time, choice);

        statisticsSize = statistics.size();
        salesReportTable.setItems(FXCollections.observableList(statistics));
        return salesReportTable;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<Sale> saleList = FXCollections.observableArrayList();
        Connection connection = DbConnection.getInstance().getConnection();
        try {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("select * from sale");

            while (resultSet.next()) {
                saleList.add(new Sale(
                                resultSet.getString("id"),
                                resultSet.getInt("percent"),
                                resultSet.getDate("started_time"),
                                resultSet.getDate("end_time"),
                                resultSet.getString("name"),
                                new Button("edit"),
                                new Button("delete")
                        )
                );
            }

            resultSet.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cb_sale.setCellFactory(new Callback<ListView<Sale>, ListCell<Sale>>() {
            @Override
            public ListCell<Sale> call(ListView<Sale> itemTypeListView) {
                final ListCell<Sale> cell = new ListCell<Sale>() {
                    @Override
                    protected void updateItem(Sale sale, boolean bln) {
                        super.updateItem(sale, bln);

                        if (sale != null) {
                            setText(sale.getName() + " " + sale.getPercent() + "%");
                        } else setText(null);
                    }
                };
                return cell;
            }
        });
        cb_sale.setButtonCell(new ListCell<Sale>() {
            @Override
            protected void updateItem(Sale sale, boolean bln) {
                super.updateItem(sale, bln);
                if (sale != null) {
                    setText(sale.getName() + " " + sale.getPercent() + "%");
                } else setText(null);
            }
        });
        cb_sale.setItems(saleList);

        String st[] = { "Combo", "Sản phẩm"};
        choiceBox.setItems(FXCollections.observableArrayList(st));
        choiceBox.getSelectionModel().selectFirst();

        choice = choiceBox.getSelectionModel().getSelectedIndex();
        indexAction();
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue ov, Number value, Number new_value)
            {
                //l1.setText(st[new_value.intValue()] + " selected");
                System.out.println(st[new_value.intValue()] + " selected");
                choice = new_value.intValue();
                indexAction();
            }
        });
    }

    private void indexAction() {
        Connection connection = DbConnection.getInstance().getConnection();
        int count = 0;
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select count(*) from invoice");
            rs.first();
            count = rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        col_invoiceId.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_customer.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        col_creator.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        col_createdTime.setCellValueFactory(new PropertyValueFactory<>("createdTime"));
        col_totalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        col_payPrice.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        col_detail.setCellValueFactory(new PropertyValueFactory<>("payPrice"));

        int pageCount = (count / 10) + 1;
        invoicePagination.setPageCount(pageCount);

        invoicePagination.setPageFactory(this::createInvoicePage);

        if (choice == 0) {
            col_s_id.setCellValueFactory(new PropertyValueFactory<>("comboId"));
            col_s_name.setCellValueFactory(new PropertyValueFactory<>("comboName"));
            col_s_creator.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
            col_s_sale.setCellValueFactory(new PropertyValueFactory<>("saleName"));
            col_s_total.setCellValueFactory(new PropertyValueFactory<>("comboQuantity"));
            col_s_revenue.setCellValueFactory(new PropertyValueFactory<>("comboTotalPrice"));
        } else {
            col_s_id.setCellValueFactory(new PropertyValueFactory<>("itemId"));
            col_s_name.setCellValueFactory(new PropertyValueFactory<>("itemName"));
            col_s_creator.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
            col_s_sale.setCellValueFactory(new PropertyValueFactory<>("saleName"));
            col_s_total.setCellValueFactory(new PropertyValueFactory<>("itemQuantity"));
            col_s_revenue.setCellValueFactory(new PropertyValueFactory<>("itemTotalPrice"));
        }

        int pageSaleReportCount = (statisticsSize / 10) + 1;
        salesReportPagination.setPageCount(pageSaleReportCount);
        salesReportPagination.setPageFactory(this::createSaleReportPage);
    }
}
