package com.server.internshipserver.service.impl.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.common.utils.ExcelUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.InternshipPost;
import com.server.internshipserver.domain.internship.dto.InternshipApplyQueryDTO;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.internship.InternshipPostMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.internship.InternshipApplyService;
import com.server.internshipserver.service.report.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.time.format.DateTimeFormatter;

/**
 * 报表管理Service实现类
 * 实现各类报表的导出功能
 */
@Service
public class ReportServiceImpl implements ReportService {
    
    @Autowired
    private InternshipApplyService internshipApplyService;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Autowired
    private InternshipPostMapper internshipPostMapper;
    
    @Override
    public void exportInternshipSummaryReport(InternshipApplyQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        // 查询所有符合条件的实习申请（不分页）
        LambdaQueryWrapper<InternshipApply> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, InternshipApply::getDeleteFlag);
        
        // 条件查询
        if (queryDTO != null) {
            if (queryDTO.getStudentId() != null) {
                wrapper.eq(InternshipApply::getStudentId, queryDTO.getStudentId());
            }
            if (queryDTO.getEnterpriseId() != null) {
                wrapper.eq(InternshipApply::getEnterpriseId, queryDTO.getEnterpriseId());
            }
            if (queryDTO.getPostId() != null) {
                wrapper.eq(InternshipApply::getPostId, queryDTO.getPostId());
            }
            if (queryDTO.getApplyType() != null) {
                wrapper.eq(InternshipApply::getApplyType, queryDTO.getApplyType());
            }
            if (queryDTO.getStatus() != null) {
                wrapper.eq(InternshipApply::getStatus, queryDTO.getStatus());
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(InternshipApply::getCreateTime);
        
        List<InternshipApply> applies = internshipApplyService.list(wrapper);
        
        // 填充关联字段
        for (InternshipApply apply : applies) {
            fillApplyRelatedFields(apply);
            
            // 转换申请类型
            if (apply.getApplyType() != null) {
                apply.setApplyTypeText(apply.getApplyType() == ApplyType.COOPERATION.getCode() ? "合作企业" : "自主实习");
            } else {
                apply.setApplyTypeText("");
            }
            
            // 转换状态文字（简化处理）
            if (apply.getStatus() != null) {
                apply.setStatusText(apply.getStatus().toString());
            } else {
                apply.setStatusText("");
            }
            
            // 转换创建时间
            if (apply.getCreateTime() != null) {
                apply.setCreateTimeText(apply.getCreateTime().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                apply.setCreateTimeText("");
            }
        }
        
        // 定义表头和字段名
        String[] headers = {"申请ID", "学号", "学生姓名", "企业名称", "岗位名称", "申请类型", "状态", "实习开始日期", "实习结束日期", "创建时间"};
        String[] fieldNames = {"applyId", "studentNo", "studentName", "enterpriseName", "postName", "applyTypeText", "statusText", "internshipStartDate", "internshipEndDate", "createTimeText"};
        
        ExcelUtil.exportToExcel(response, applies, headers, fieldNames, "实习情况汇总表");
    }
    
    /**
     * 填充申请关联字段
     */
    private void fillApplyRelatedFields(InternshipApply apply) {
        // 填充学生信息
        if (apply.getStudentId() != null) {
            Student student = studentMapper.selectById(apply.getStudentId());
            if (student != null) {
                apply.setStudentNo(student.getStudentNo());
                // 获取用户信息
                if (student.getUserId() != null) {
                    UserInfo user = userMapper.selectById(student.getUserId());
                    if (user != null) {
                        apply.setStudentName(user.getRealName());
                    }
                }
            }
        }
        
        // 填充企业信息
        if (apply.getEnterpriseId() != null) {
            // 合作企业申请，从企业表获取企业信息
            Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
            if (enterprise != null) {
                apply.setEnterpriseName(enterprise.getEnterpriseName());
            }
        } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
            // 自主实习，使用自主实习企业信息
            apply.setEnterpriseName(apply.getSelfEnterpriseName());
        }
        
        // 填充岗位信息
        if (apply.getPostId() != null) {
            // 合作企业申请，从岗位表获取岗位信息
            InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
            if (post != null) {
                apply.setPostName(post.getPostName());
            }
        } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
            // 自主实习，使用自主实习岗位名称
            apply.setPostName(apply.getSelfPostName());
        }
    }
}
