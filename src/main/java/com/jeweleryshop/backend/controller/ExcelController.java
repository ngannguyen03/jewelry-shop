package com.jeweleryshop.backend.controller;

import com.jeweleryshop.backend.payload.response.ResponseMessage;
import com.jeweleryshop.backend.service.ExcelService;
import com.jeweleryshop.backend.utils.ExcelHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/excel")
@PreAuthorize("hasRole('ADMIN')")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    @PostMapping("/import-products")
    public ResponseEntity<ResponseMessage> importProducts(@RequestParam("file") MultipartFile file) {
        String message = "";

        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                int importedCount = excelService.getImportedCount(file);
                excelService.importProductsFromExcel(file);
                message = "Import thành công " + importedCount + " sản phẩm từ file: " + file.getOriginalFilename();
                return ResponseEntity.ok(new ResponseMessage(message));
            } catch (Exception e) {
                message = "Lỗi khi import file: " + file.getOriginalFilename() + "! " + e.getMessage();
                return ResponseEntity.badRequest().body(new ResponseMessage(message));
            }
        }

        message = "Vui lòng upload file Excel (định dạng .xlsx)!";
        return ResponseEntity.badRequest().body(new ResponseMessage(message));
    }

    @GetMapping("/export-products")
    public ResponseEntity<Resource> exportProducts() {
        String filename = "danh-sach-san-pham-" + System.currentTimeMillis() + ".xlsx";
        InputStreamResource file = new InputStreamResource(excelService.exportProductsToExcel());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadTemplate() {
        String filename = "template-san-pham.xlsx";
        InputStreamResource file = new InputStreamResource(ExcelHelper.productsToExcel(java.util.Collections.emptyList()));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
