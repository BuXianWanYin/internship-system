package com.server.internshipserver.common.utils;

import com.server.internshipserver.domain.user.dto.StudentImportDTO;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel工具类
 * 提供Excel文件的解析和生成功能，主要用于学生数据的导入导出
 */
public class ExcelUtil {
    
    /**
     * 解析学生导入Excel文件
     * @param file Excel文件
     * @return 学生导入数据列表
     * @throws IOException IO异常
     */
    public static List<StudentImportDTO> parseStudentImportExcel(MultipartFile file) throws IOException {
        List<StudentImportDTO> studentList = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return studentList;
            }
            
            // 从第二行开始读取（第一行是表头）
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    continue;
                }
                
                StudentImportDTO dto = new StudentImportDTO();
                dto.setRowNum(rowNum + 1); // Excel行号从1开始
                
                // 读取各列数据
                // 列顺序：学号、姓名、身份证号、手机号、邮箱、入学年份、班级ID、密码（可选）
                try {
                    dto.setStudentNo(getCellValue(row.getCell(0)));
                    dto.setRealName(getCellValue(row.getCell(1)));
                    dto.setIdCard(getCellValue(row.getCell(2)));
                    dto.setPhone(getCellValue(row.getCell(3)));
                    dto.setEmail(getCellValue(row.getCell(4)));
                    
                    // 入学年份
                    String enrollmentYearStr = getCellValue(row.getCell(5));
                    if (enrollmentYearStr != null && !enrollmentYearStr.isEmpty()) {
                        try {
                            dto.setEnrollmentYear(Integer.parseInt(enrollmentYearStr.trim()));
                        } catch (NumberFormatException e) {
                            dto.setErrorMessage("入学年份格式错误");
                        }
                    }
                    
                    // 班级ID
                    String classIdStr = getCellValue(row.getCell(6));
                    if (classIdStr != null && !classIdStr.isEmpty()) {
                        try {
                            dto.setClassId(Long.parseLong(classIdStr.trim()));
                        } catch (NumberFormatException e) {
                            dto.setErrorMessage("班级ID格式错误");
                        }
                    }
                    
                    // 密码（可选，第8列，索引7）
                    String password = getCellValue(row.getCell(7));
                    if (password != null && !password.trim().isEmpty()) {
                        dto.setPassword(password.trim());
                    }
                    
                    // 如果学号或姓名为空，跳过该行
                    if (dto.getStudentNo() == null || dto.getStudentNo().trim().isEmpty() ||
                        dto.getRealName() == null || dto.getRealName().trim().isEmpty()) {
                        continue;
                    }
                    
                    studentList.add(dto);
                } catch (Exception e) {
                    dto.setErrorMessage("数据解析错误: " + e.getMessage());
                    studentList.add(dto);
                }
            }
        }
        
        return studentList;
    }
    
    /**
     * 获取单元格值（转换为字符串）
     * 支持字符串、数字、日期、布尔值、公式等类型的单元格
     * 
     * @param cell Excel单元格对象
     * @return 单元格值的字符串表示，如果单元格为空则返回null
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return String.valueOf(cell.getDateCellValue());
                } else {
                    // 处理数字，避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    /**
     * 生成学生导入Excel模板
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    public static void generateStudentImportTemplate(HttpServletResponse response) throws IOException {
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("学生导入模板");
        
        // 创建表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
        // 创建表头行
        Row headerRow = sheet.createRow(0);
        // 列顺序：学号、姓名、身份证号、手机号、邮箱、入学年份、班级ID、密码（可选）
        String[] headers = {"学号*", "姓名*", "身份证号", "手机号", "邮箱", "入学年份*", "班级ID*", "初始密码（可选，不填则自动生成8位随机数字）"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 设置列宽
        sheet.setColumnWidth(0, 4000); // 学号
        sheet.setColumnWidth(1, 3000); // 姓名
        sheet.setColumnWidth(2, 5000); // 身份证号
        sheet.setColumnWidth(3, 4000); // 手机号
        sheet.setColumnWidth(4, 5000); // 邮箱
        sheet.setColumnWidth(5, 3000); // 入学年份
        sheet.setColumnWidth(6, 3000); // 班级ID
        sheet.setColumnWidth(7, 6000); // 密码
        
        // 添加示例数据行
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("202101001");
        exampleRow.createCell(1).setCellValue("张三");
        exampleRow.createCell(2).setCellValue("110101199001011234");
        exampleRow.createCell(3).setCellValue("13800000001");
        exampleRow.createCell(4).setCellValue("zhangsan@example.com");
        exampleRow.createCell(5).setCellValue("2021");
        exampleRow.createCell(6).setCellValue("1");
        exampleRow.createCell(7).setCellValue("123456"); // 示例密码，可选
        
        // 设置响应头
        String fileName = URLEncoder.encode("学生导入模板.xlsx", StandardCharsets.UTF_8.toString());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        
        // 写入响应流
        try (OutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
            outputStream.flush();
        } finally {
            workbook.close();
        }
    }
}

