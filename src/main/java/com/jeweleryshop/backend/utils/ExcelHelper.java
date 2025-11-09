package com.jeweleryshop.backend.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import com.jeweleryshop.backend.entity.Category;
import com.jeweleryshop.backend.entity.Product;

public class ExcelHelper {

    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String[] HEADERs = {
        "ID", "Name", "Description", "Base Price", "Discount Price",
        "SKU Prefix", "Category", "Active", "Image URL"
    };
    static String SHEET = "Products";

    // 👉 Thêm cấu hình cho sheet Category (dùng cho export 2 sheet)
    static String CATEGORY_SHEET = "Categories";
    static String[] CATEGORY_HEADERS = {
        "ID", "Category Name", "Description", "Active"
    };

    public static boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    /**
     * ✅ Hàm cũ: Export chỉ 1 sheet Products (GIỮ LẠI cho tương thích)
     */
    public static ByteArrayInputStream productsToExcel(List<Product> products) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET);

            // Header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Header
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERs.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERs[col]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            int rowIdx = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(product.getId() != null ? product.getId() : 0);
                row.createCell(1).setCellValue(nvl(product.getName()));
                row.createCell(2).setCellValue(nvl(product.getDescription()));
                row.createCell(3).setCellValue(product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0);
                row.createCell(4).setCellValue(product.getDiscountPrice() != null ? product.getDiscountPrice().doubleValue() : 0);
                row.createCell(5).setCellValue(nvl(product.getSkuPrefix()));
                row.createCell(6).setCellValue(product.getCategory() != null ? nvl(product.getCategory().getName()) : "");
                row.createCell(7).setCellValue(product.getIsActive() != null ? product.getIsActive() : true);
                row.createCell(8).setCellValue(nvl(product.getImageUrl()));
            }

            // Auto-size columns
            for (int i = 0; i < HEADERs.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi export Excel: " + e.getMessage());
        }
    }

    /**
     * ✅ Hàm mới: Export 2 sheet: Products + Categories
     */
    public static ByteArrayInputStream productsAndCategoriesToExcel(
            List<Product> products,
            List<Category> categories
    ) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);

            // ===== SHEET 1: PRODUCTS =====
            Sheet productSheet = workbook.createSheet(SHEET);

            Row productHeader = productSheet.createRow(0);
            for (int col = 0; col < HEADERs.length; col++) {
                Cell cell = productHeader.createCell(col);
                cell.setCellValue(HEADERs[col]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Product product : products) {
                Row row = productSheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(product.getId() != null ? product.getId() : 0);
                row.createCell(1).setCellValue(nvl(product.getName()));
                row.createCell(2).setCellValue(nvl(product.getDescription()));
                row.createCell(3).setCellValue(product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0);
                row.createCell(4).setCellValue(product.getDiscountPrice() != null ? product.getDiscountPrice().doubleValue() : 0);
                row.createCell(5).setCellValue(nvl(product.getSkuPrefix()));
                row.createCell(6).setCellValue(product.getCategory() != null ? nvl(product.getCategory().getName()) : "");
                row.createCell(7).setCellValue(product.getIsActive() != null ? product.getIsActive() : true);
                row.createCell(8).setCellValue(nvl(product.getImageUrl()));
            }

            for (int i = 0; i < HEADERs.length; i++) {
                productSheet.autoSizeColumn(i);
            }

            // ===== SHEET 2: CATEGORIES =====
            Sheet categorySheet = workbook.createSheet(CATEGORY_SHEET);

            Row catHeader = categorySheet.createRow(0);
            for (int col = 0; col < CATEGORY_HEADERS.length; col++) {
                Cell cell = catHeader.createCell(col);
                cell.setCellValue(CATEGORY_HEADERS[col]);
                cell.setCellStyle(headerStyle);
            }

            int catIdx = 1;
            for (Category c : categories) {
                Row row = categorySheet.createRow(catIdx++);

                row.createCell(0).setCellValue(c.getId() != null ? c.getId() : 0);
                row.createCell(1).setCellValue(nvl(c.getName()));
                row.createCell(2).setCellValue(nvl(c.getDescription()));
                // tuỳ entity của bạn là getIsActive() hay getActive()
                Boolean active = null;
                try {
                    active = (Boolean) Category.class.getMethod("getIsActive").invoke(c);
                } catch (Exception e) {
                    try {
                        active = (Boolean) Category.class.getMethod("getActive").invoke(c);
                    } catch (Exception ignored) {
                    }
                }
                row.createCell(3).setCellValue(active != null ? active : true);
            }

            for (int i = 0; i < CATEGORY_HEADERS.length; i++) {
                categorySheet.autoSizeColumn(i);
            }

            // Ghi ra output
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi export Excel (2 sheet): " + e.getMessage());
        }
    }

    // ================= IMPORT (GIỮ NGUYÊN) =================
    public static List<Product> excelToProducts(InputStream inputStream, List<Category> categories) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheet(SHEET);
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            Iterator<Row> rows = sheet.iterator();
            List<Product> products = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();
                Product product = new Product();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    switch (cellIdx) {
                        case 0: // ID (bỏ qua khi import)
                            break;
                        case 1: // Name
                            product.setName(getStringValue(currentCell));
                            break;
                        case 2: // Description
                            product.setDescription(getStringValue(currentCell));
                            break;
                        case 3: // Base Price
                            double basePrice = getNumericValue(currentCell);
                            if (basePrice >= 0) {
                                product.setBasePrice(BigDecimal.valueOf(basePrice));
                            }
                            break;
                        case 4: // Discount Price
                            double discountPrice = getNumericValue(currentCell);
                            if (discountPrice > 0) {
                                product.setDiscountPrice(BigDecimal.valueOf(discountPrice));
                            }
                            break;
                        case 5: // SKU Prefix
                            product.setSkuPrefix(getStringValue(currentCell));
                            break;
                        case 6: // Category
                            String categoryName = getStringValue(currentCell);
                            if (!categoryName.isEmpty()) {
                                Optional<Category> category = categories.stream()
                                        .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                                        .findFirst();
                                category.ifPresent(product::setCategory);
                                // nếu muốn tự tạo category mới thì xử lý ở Service
                            }
                            break;
                        case 7: // Active
                            product.setIsActive(getBooleanValue(currentCell));
                            break;
                        case 8: // Image URL
                            product.setImageUrl(getStringValue(currentCell));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }

                if (product.getIsActive() == null) {
                    product.setIsActive(true);
                }

                product.setCreatedAt(LocalDateTime.now());
                product.setUpdatedAt(LocalDateTime.now());

                if (product.getName() != null
                        && !product.getName().trim().isEmpty()
                        && product.getBasePrice() != null) {
                    products.add(product);
                }
            }

            return products;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi import Excel: " + e.getMessage());
        }
    }

    // ================= HELPER METHODS =================
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String getStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                switch (cell.getCachedFormulaResultType()) {
                    case STRING:
                        return cell.getStringCellValue().trim();
                    case NUMERIC:
                        return String.valueOf(cell.getNumericCellValue());
                    case BOOLEAN:
                        return String.valueOf(cell.getBooleanCellValue());
                    default:
                        return "";
                }
            default:
                return "";
        }
    }

    private static double getNumericValue(Cell cell) {
        if (cell == null) {
            return 0.0;
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }

    private static Boolean getBooleanValue(Cell cell) {
        if (cell == null) {
            return true;
        }

        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case STRING:
                String value = cell.getStringCellValue().trim().toLowerCase();
                return value.equals("true") || value.equals("1") || value.equals("yes")
                        || value.equals("active") || value.equals("có");
            case NUMERIC:
                return cell.getNumericCellValue() == 1;
            case FORMULA:
                try {
                    return cell.getBooleanCellValue();
                } catch (Exception e) {
                    String formulaValue = getStringValue(cell).toLowerCase();
                    return formulaValue.equals("true") || formulaValue.equals("1")
                            || formulaValue.equals("yes") || formulaValue.equals("active");
                }
            default:
                return true;
        }
    }
}
