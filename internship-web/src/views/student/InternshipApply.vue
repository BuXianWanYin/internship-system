<template>
  <PageLayout title="实习申请">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 合作企业申请 -->
      <el-tab-pane label="合作企业申请" name="cooperation">
        <el-tabs v-model="cooperationSubTab" @tab-change="handleCooperationSubTabChange">
          <!-- 岗位列表 -->
          <el-tab-pane label="岗位列表" name="postList">
        <div class="apply-section">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 20px"
          >
            <template #default>
              <div>您可以选择与学校有合作关系的企业发布的岗位进行申请</div>
            </template>
          </el-alert>

          <!-- 搜索栏 -->
          <div class="search-bar">
            <el-form :inline="true" :model="searchForm" class="search-form">
              <el-form-item label="岗位名称">
                <el-input
                  v-model="searchForm.postName"
                  placeholder="请输入岗位名称"
                  clearable
                  style="width: 200px"
                  @keyup.enter="handleSearch"
                />
              </el-form-item>
              <el-form-item label="企业名称">
                <el-input
                  v-model="searchForm.enterpriseName"
                  placeholder="请输入企业名称"
                  clearable
                  style="width: 200px"
                  @keyup.enter="handleSearch"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
                <el-button :icon="Refresh" @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 岗位列表 -->
          <el-table
            v-loading="loading"
            :data="postTableData"
            stripe
            style="width: 100%"
            :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
          >
            <el-table-column prop="postName" label="岗位名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="enterpriseName" label="企业名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="workLocation" label="工作地点" min-width="150" />
            <el-table-column prop="recruitCount" label="招聘人数" width="100" align="center" />
            <el-table-column prop="appliedCount" label="已申请" width="100" align="center" />
            <el-table-column label="薪资范围" width="150" align="center">
              <template #default="{ row }">
                <span v-if="row.salaryMin && row.salaryMax">
                  {{ row.salaryMin }}-{{ row.salaryMax }}元/{{ row.salaryType || '月' }}
                </span>
                <span v-else-if="row.salaryType === '面议'">面议</span>
                <span v-else style="color: #909399">未设置</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right" align="center">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleViewPost(row)">查看详情</el-button>
                <el-button link type="success" size="small" @click="handleApplyPost(row)">申请</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="postPagination.current"
              v-model:page-size="postPagination.size"
              :total="postPagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handlePostSizeChange"
              @current-change="handlePostPageChange"
            />
          </div>
        </div>
          </el-tab-pane>

          <!-- 我的申请 -->
          <el-tab-pane label="我的申请" name="myApply">
            <div class="apply-section">
              <el-table
                v-loading="cooperationApplyLoading"
                :data="cooperationApplyTableData"
                stripe
                style="width: 100%"
                :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
              >
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="postName" label="岗位名称" min-width="150" show-overflow-tooltip />
                <el-table-column prop="enterpriseName" label="企业名称" min-width="200" show-overflow-tooltip />
                <el-table-column prop="status" label="状态" width="150" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getApplyStatusType(row.status, row.statusText)" size="small">
                      {{ row.statusText || getApplyStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="申请时间" width="180">
                  <template #default="{ row }">
                    {{ formatDateTime(row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="280" fixed="right" align="center">
                  <template #default="{ row }">
                    <el-button link type="primary" size="small" @click="handleViewCooperationApply(row)">查看详情</el-button>
                    <el-button
                      v-if="row.status === 0"
                      link
                      type="danger"
                      size="small"
                      @click="handleCancelCooperationApply(row)"
                    >
                      取消
                    </el-button>
                    <el-button
                      v-if="row.status === 3 && (row.studentConfirmStatus === 0 || row.studentConfirmStatus === null)"
                      link
                      type="warning"
                      size="small"
                      :loading="confirmOnboardLoading === row.applyId"
                      @click="handleConfirmOnboard(row)"
                    >
                      确认上岗
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 分页 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="cooperationApplyPagination.current"
                  v-model:page-size="cooperationApplyPagination.size"
                  :total="cooperationApplyPagination.total"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleCooperationApplySizeChange"
                  @current-change="handleCooperationApplyPageChange"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-tab-pane>

      <!-- 自主实习申请 -->
      <el-tab-pane label="自主实习申请" name="self">
        <div class="apply-section">
          <el-alert
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 20px"
          >
            <template #default>
              <div>自主实习需要填写企业信息，提交后需要等待审核</div>
            </template>
          </el-alert>

          <!-- 我的申请列表 -->
          <el-table
            v-loading="applyLoading"
            :data="applyTableData"
            stripe
            style="width: 100%"
            :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
          >
            <el-table-column prop="enterpriseName" label="企业名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="enterpriseAddress" label="企业地址" min-width="200" show-overflow-tooltip />
            <el-table-column prop="contactPerson" label="联系人" min-width="120" />
            <el-table-column prop="contactPhone" label="联系电话" min-width="120" />
            <el-table-column prop="status" label="状态" width="150" align="center">
              <template #default="{ row }">
                    <el-tag :type="getApplyStatusType(row.status, row.statusText)" size="small">
                      {{ row.statusText || getApplyStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right" align="center">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleViewApply(row)">查看</el-button>
                <el-button
                  v-if="row.status === 0"
                  link
                  type="danger"
                  size="small"
                  @click="handleCancelApply(row)"
                >
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="applyPagination.current"
              v-model:page-size="applyPagination.size"
              :total="applyPagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleApplySizeChange"
              @current-change="handleApplyPageChange"
            />
          </div>

          <!-- 添加自主实习申请按钮 -->
          <div style="margin-top: 20px">
            <el-button type="primary" :icon="Plus" @click="handleAddSelfApply">添加自主实习申请</el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 岗位详情对话框 -->
    <el-dialog
      v-model="postDetailDialogVisible"
      title="岗位详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ postDetailData.postName }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ postDetailData.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="工作地点">{{ postDetailData.workLocation }}</el-descriptions-item>
        <el-descriptions-item label="详细地址">{{ postDetailData.workAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="招聘人数">{{ postDetailData.recruitCount }}</el-descriptions-item>
        <el-descriptions-item label="已申请人数">{{ postDetailData.appliedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">
          <span v-if="postDetailData.salaryMin && postDetailData.salaryMax">
            {{ postDetailData.salaryMin }}-{{ postDetailData.salaryMax }}元/{{ postDetailData.salaryType || '月' }}
          </span>
          <span v-else-if="postDetailData.salaryType === '面议'">面议</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="工作时间">{{ postDetailData.workHours || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实习开始日期">
          {{ postDetailData.internshipStartDate ? formatDate(postDetailData.internshipStartDate) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实习结束日期">
          {{ postDetailData.internshipEndDate ? formatDate(postDetailData.internshipEndDate) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="岗位描述" :span="2">
          <div style="white-space: pre-wrap">{{ postDetailData.postDescription || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="技能要求" :span="2">
          <div style="white-space: pre-wrap">{{ postDetailData.skillRequirements || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="postDetailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleApplyFromPostDetail">申请该岗位</el-button>
      </template>
    </el-dialog>

    <!-- 申请岗位对话框 -->
    <el-dialog
      v-model="applyPostDialogVisible"
      title="提交实习申请"
      width="700px"
    >
      <el-form
        ref="applyPostFormRef"
        :model="applyPostForm"
        :rules="applyPostFormRules"
        label-width="120px"
      >
        <el-form-item label="申请岗位">
          <el-input :value="applyPostForm.postName" disabled />
        </el-form-item>
        <el-form-item label="申请企业">
          <el-input :value="applyPostForm.enterpriseName" disabled />
        </el-form-item>
        <!-- 新增：选择实习计划 -->
        <el-form-item label="实习计划" prop="planId" required>
          <el-select
            v-model="applyPostForm.planId"
            placeholder="请选择实习计划"
            style="width: 100%"
            :loading="loadingPlans"
            @change="handlePlanChange"
          >
            <el-option
              v-for="plan in availablePlans"
              :key="plan.planId"
              :label="`${plan.planName} (${plan.planCode})`"
              :value="plan.planId"
            >
              <div>
                <div>
                  {{ plan.planName }}
                  <el-tag v-if="plan.planScopeType" :type="getPlanScopeTypeTag(plan.planScopeType)" size="small" style="margin-left: 8px;">
                    {{ plan.planScopeType }}
                  </el-tag>
                </div>
                <div style="font-size: 12px; color: #999;">
                  {{ formatDate(plan.startDate) }} ~ {{ formatDate(plan.endDate) }}
                </div>
              </div>
            </el-option>
          </el-select>
          <el-empty v-if="!loadingPlans && availablePlans.length === 0" description="暂无可用的实习计划" :image-size="60" style="margin-top: 10px" />
        </el-form-item>
        
        <!-- 显示计划详情 -->
        <el-form-item v-if="selectedPlan" label="计划详情">
          <el-card shadow="never" style="margin-top: 10px;">
            <div v-if="selectedPlan.planOutline" style="margin-bottom: 10px;">
              <h4 style="margin: 0 0 8px 0; font-size: 14px;">实习大纲：</h4>
              <div v-html="selectedPlan.planOutline" style="font-size: 13px; color: #606266;"></div>
            </div>
            <div v-if="selectedPlan.taskRequirements" style="margin-bottom: 10px;">
              <h4 style="margin: 0 0 8px 0; font-size: 14px;">任务要求：</h4>
              <div v-html="selectedPlan.taskRequirements" style="font-size: 13px; color: #606266;"></div>
            </div>
            <div v-if="selectedPlan.assessmentStandards">
              <h4 style="margin: 0 0 8px 0; font-size: 14px;">考核标准：</h4>
              <div v-html="selectedPlan.assessmentStandards" style="font-size: 13px; color: #606266;"></div>
            </div>
          </el-card>
        </el-form-item>
        
        <!-- 实习时间（根据计划自动填充，可手动调整） -->
        <el-form-item label="实习开始日期" prop="internshipStartDate">
          <el-date-picker
            v-model="applyPostForm.internshipStartDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            :disabled-date="(date) => disabledStartDate(date)"
          />
        </el-form-item>
        <el-form-item label="实习结束日期" prop="internshipEndDate">
          <el-date-picker
            v-model="applyPostForm.internshipEndDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
            :disabled-date="(date) => disabledEndDate(date)"
          />
        </el-form-item>
        
        <el-form-item label="简历附件" prop="resumeAttachment">
          <el-upload
            ref="resumeUploadRef"
            v-model:file-list="resumeFileList"
            :auto-upload="false"
            :limit="5"
            :on-change="handleResumeFileChange"
            :on-remove="handleResumeFileRemove"
            :before-upload="beforeResumeUpload"
            accept=".doc,.docx,.pdf"
            multiple
          >
            <el-button type="primary" :icon="Upload">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传Word文档（.doc, .docx）和PDF文件（.pdf），最多5个文件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
          <div v-if="resumeAttachmentUrls.length > 0" class="attachment-list" style="margin-top: 10px">
            <div v-for="(url, index) in resumeAttachmentUrls" :key="index" class="attachment-item" style="display: flex; align-items: center; margin-bottom: 8px">
              <el-icon style="margin-right: 8px"><Document /></el-icon>
              <span style="flex: 1; margin-right: 8px">{{ getResumeFileName(url) }}</span>
              <el-button link type="primary" size="small" @click="handleDownloadResume(url)">下载</el-button>
              <el-button link type="danger" size="small" @click="removeResumeAttachment(index)">删除</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="申请理由" prop="applyReason">
          <el-input
            v-model="applyPostForm.applyReason"
            type="textarea"
            :rows="4"
            placeholder="请输入申请理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyPostDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyPostLoading" @click="handleSubmitPostApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 自主实习申请对话框 -->
    <el-dialog
      v-model="selfApplyDialogVisible"
      :title="selfApplyDialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="selfApplyFormRef"
        :model="selfApplyForm"
        :rules="selfApplyFormRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="enterpriseName">
              <el-input v-model="selfApplyForm.enterpriseName" placeholder="请输入企业名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="unifiedSocialCreditCode">
              <el-input v-model="selfApplyForm.unifiedSocialCreditCode" placeholder="请输入统一社会信用代码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="企业地址" prop="enterpriseAddress">
          <el-input
            v-model="selfApplyForm.enterpriseAddress"
            type="textarea"
            :rows="2"
            placeholder="请输入企业地址"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属行业" prop="industry">
              <el-input v-model="selfApplyForm.industry" placeholder="请输入所属行业" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业规模" prop="enterpriseScale">
              <el-select v-model="selfApplyForm.enterpriseScale" placeholder="请选择企业规模" style="width: 100%">
                <el-option label="大型企业（500人以上）" value="大型企业" />
                <el-option label="中型企业（100-500人）" value="中型企业" />
                <el-option label="小型企业（20-100人）" value="小型企业" />
                <el-option label="微型企业（20人以下）" value="微型企业" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="selfApplyForm.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="selfApplyForm.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="selfApplyForm.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
        <!-- 新增：选择实习计划 -->
        <el-form-item label="实习计划" prop="planId" required>
          <el-select
            v-model="selfApplyForm.planId"
            placeholder="请选择实习计划"
            style="width: 100%"
            :loading="loadingPlans"
            @change="handleSelfApplyPlanChange"
          >
            <el-option
              v-for="plan in availablePlans"
              :key="plan.planId"
              :label="`${plan.planName} (${plan.planCode})`"
              :value="plan.planId"
            >
              <div>
                <div>
                  {{ plan.planName }}
                  <el-tag v-if="plan.planScopeType" :type="getPlanScopeTypeTag(plan.planScopeType)" size="small" style="margin-left: 8px;">
                    {{ plan.planScopeType }}
                  </el-tag>
                </div>
                <div style="font-size: 12px; color: #999;">
                  {{ formatDate(plan.startDate) }} ~ {{ formatDate(plan.endDate) }}
                </div>
              </div>
            </el-option>
          </el-select>
          <el-empty v-if="!loadingPlans && availablePlans.length === 0" description="暂无可用的实习计划" :image-size="60" style="margin-top: 10px" />
        </el-form-item>
        
        <!-- 显示计划详情 -->
        <el-form-item v-if="selectedSelfApplyPlan" label="计划详情">
          <el-card shadow="never" style="margin-top: 10px;">
            <div v-if="selectedSelfApplyPlan.planOutline" style="margin-bottom: 10px;">
              <h4 style="margin: 0 0 8px 0; font-size: 14px;">实习大纲：</h4>
              <div v-html="selectedSelfApplyPlan.planOutline" style="font-size: 13px; color: #606266;"></div>
            </div>
            <div v-if="selectedSelfApplyPlan.taskRequirements" style="margin-bottom: 10px;">
              <h4 style="margin: 0 0 8px 0; font-size: 14px;">任务要求：</h4>
              <div v-html="selectedSelfApplyPlan.taskRequirements" style="font-size: 13px; color: #606266;"></div>
            </div>
            <div v-if="selectedSelfApplyPlan.assessmentStandards">
              <h4 style="margin: 0 0 8px 0; font-size: 14px;">考核标准：</h4>
              <div v-html="selectedSelfApplyPlan.assessmentStandards" style="font-size: 13px; color: #606266;"></div>
            </div>
          </el-card>
        </el-form-item>
        
        <el-form-item label="实习岗位" prop="internshipPosition">
          <el-input v-model="selfApplyForm.internshipPosition" placeholder="请输入实习岗位名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="实习开始日期" prop="internshipStartDate">
              <el-date-picker
                v-model="selfApplyForm.internshipStartDate"
                type="date"
                placeholder="请选择开始日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                :disabled-date="(date) => disabledSelfApplyStartDate(date)"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实习结束日期" prop="internshipEndDate">
              <el-date-picker
                v-model="selfApplyForm.internshipEndDate"
                type="date"
                placeholder="请选择结束日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                :disabled-date="(date) => disabledSelfApplyEndDate(date)"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="申请理由" prop="applyReason">
          <el-input
            v-model="selfApplyForm.applyReason"
            type="textarea"
            :rows="4"
            placeholder="请输入申请理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="selfApplyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="selfApplyLoading" @click="handleSubmitSelfApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 查看自主实习申请详情对话框 -->
    <el-dialog
      v-model="applyDetailDialogVisible"
      title="申请详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="企业名称">{{ applyDetailData.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ applyDetailData.unifiedSocialCreditCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实习计划" :span="2">
          <div v-if="applyDetailData.planName">
            <div>{{ applyDetailData.planName }}</div>
            <div style="font-size: 12px; color: #999;">{{ applyDetailData.planCode }}</div>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="企业地址" :span="2">{{ applyDetailData.enterpriseAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属行业">{{ applyDetailData.industry || '-' }}</el-descriptions-item>
        <el-descriptions-item label="企业规模">{{ applyDetailData.enterpriseScale || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ applyDetailData.contactPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ applyDetailData.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ applyDetailData.contactEmail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实习岗位">{{ applyDetailData.internshipPosition || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实习开始日期">
          {{ applyDetailData.internshipStartDate ? formatDate(applyDetailData.internshipStartDate) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实习结束日期">
          {{ applyDetailData.internshipEndDate ? formatDate(applyDetailData.internshipEndDate) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getApplyStatusType(applyDetailData.status, applyDetailData.statusText)" size="small">
            {{ applyDetailData.statusText || getApplyStatusText(applyDetailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(applyDetailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="applyDetailData.resumeAttachment" label="简历附件" :span="2">
          <div class="attachment-list">
            <div v-for="(url, index) in (applyDetailData.resumeAttachment || '').split(',').filter(u => u)" :key="index" class="attachment-item" style="display: flex; align-items: center; margin-bottom: 8px">
              <el-icon style="margin-right: 8px"><Document /></el-icon>
              <span style="flex: 1; margin-right: 8px">{{ getResumeFileName(url) }}</span>
              <el-button link type="primary" size="small" @click="handleDownloadResume(url)">下载</el-button>
            </div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="申请理由" :span="2">
          <div style="white-space: pre-wrap">{{ applyDetailData.applyReason || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="applyDetailData.auditOpinion" label="审核意见" :span="2">
          <div style="white-space: pre-wrap">{{ applyDetailData.auditOpinion }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 状态流转历史 -->
      <el-divider content-position="left">状态流转历史</el-divider>
      <el-timeline v-if="applyDetailData.statusHistory && applyDetailData.statusHistory.length > 0">
        <el-timeline-item
          v-for="(item, index) in applyDetailData.statusHistory"
          :key="index"
          :timestamp="formatDateTime(item.actionTime)"
          placement="top"
        >
          <el-card>
            <h4>{{ item.actionName }}</h4>
            <p><strong>操作人：</strong>{{ item.operator || '-' }}</p>
            <p><strong>说明：</strong>{{ item.description || '-' }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无状态流转记录" :image-size="80" />
      
      <!-- 下一步操作提示 -->
      <el-divider content-position="left">下一步操作</el-divider>
      <el-alert
        v-if="applyDetailData.nextActionTip"
        :title="applyDetailData.nextActionTip"
        type="info"
        :closable="false"
        show-icon
        style="margin-top: 10px"
      />
    </el-dialog>

    <!-- 合作企业申请详情对话框 -->
    <el-dialog
      v-model="cooperationApplyDetailDialogVisible"
      title="申请详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ cooperationApplyDetailData.postName }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ cooperationApplyDetailData.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getApplyStatusType(cooperationApplyDetailData.status, cooperationApplyDetailData.statusText)" size="small">
            {{ cooperationApplyDetailData.statusText || getApplyStatusText(cooperationApplyDetailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(cooperationApplyDetailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="cooperationApplyDetailData.resumeAttachment" label="简历附件" :span="2">
          <div class="attachment-list">
            <div v-for="(url, index) in (cooperationApplyDetailData.resumeAttachment || '').split(',').filter(u => u)" :key="index" class="attachment-item" style="display: flex; align-items: center; margin-bottom: 8px">
              <el-icon style="margin-right: 8px"><Document /></el-icon>
              <span style="flex: 1; margin-right: 8px">{{ getResumeFileName(url) }}</span>
              <el-button link type="primary" size="small" @click="handleDownloadResume(url)">下载</el-button>
            </div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="申请理由" :span="2">
          <div style="white-space: pre-wrap">{{ cooperationApplyDetailData.applyReason || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="cooperationApplyDetailData.auditOpinion" label="审核意见" :span="2">
          <div style="white-space: pre-wrap">{{ cooperationApplyDetailData.auditOpinion }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 状态流转历史 -->
      <el-divider content-position="left">状态流转历史</el-divider>
      <el-timeline v-if="cooperationApplyDetailData.statusHistory && cooperationApplyDetailData.statusHistory.length > 0">
        <el-timeline-item
          v-for="(item, index) in cooperationApplyDetailData.statusHistory"
          :key="index"
          :timestamp="formatDateTime(item.actionTime)"
          placement="top"
        >
          <el-card>
            <h4>{{ item.actionName }}</h4>
            <p><strong>操作人：</strong>{{ item.operator || '-' }}</p>
            <p><strong>说明：</strong>{{ item.description || '-' }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无状态流转记录" :image-size="80" />
      
      <!-- 下一步操作提示 -->
      <el-divider content-position="left">下一步操作</el-divider>
      <el-alert
        v-if="cooperationApplyDetailData.nextActionTip"
        :title="cooperationApplyDetailData.nextActionTip"
        type="info"
        :closable="false"
        show-icon
        style="margin-top: 10px"
      />
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Upload, Document } from '@element-plus/icons-vue'
import { postApi } from '@/api/internship/post'
import { applyApi } from '@/api/internship/apply'
import { planApi } from '@/api/internship/plan'
import { fileApi } from '@/api/common/file'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

// 获取计划范围类型标签类型
const getPlanScopeTypeTag = (planScopeType) => {
  if (planScopeType === '全校计划') {
    return 'info'
  } else if (planScopeType === '全院计划') {
    return 'warning'
  } else if (planScopeType === '专业计划') {
    return 'success'
  }
  return 'info'
}

const activeTab = ref('cooperation')
const cooperationSubTab = ref('postList')
const loading = ref(false)
const applyLoading = ref(false)
const cooperationApplyLoading = ref(false)
const applyPostLoading = ref(false)
const selfApplyLoading = ref(false)
const confirmOnboardLoading = ref(null)
const postDetailDialogVisible = ref(false)
const applyPostDialogVisible = ref(false)
const selfApplyDialogVisible = ref(false)
const applyDetailDialogVisible = ref(false)
const cooperationApplyDetailDialogVisible = ref(false)
const selfApplyDialogTitle = ref('添加自主实习申请')
const applyPostFormRef = ref(null)
const selfApplyFormRef = ref(null)

const searchForm = reactive({
  postName: '',
  enterpriseName: ''
})

const postPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const applyPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const cooperationApplyPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const postTableData = ref([])
const applyTableData = ref([])
const cooperationApplyTableData = ref([])
const postDetailData = ref({})
const applyDetailData = ref({})
const cooperationApplyDetailData = ref({})

const applyPostForm = reactive({
  planId: null,
  enterpriseId: null,
  postId: null,
  enterpriseName: '',
  postName: '',
  applyReason: '',
  internshipStartDate: null,
  internshipEndDate: null
})

const resumeUploadRef = ref(null)
const resumeFileList = ref([])
const resumeAttachmentUrls = ref([])

// 新增：可用计划列表
const availablePlans = ref([])
const selectedPlan = ref(null)
const loadingPlans = ref(false)

const selfApplyForm = reactive({
  applyId: null,
  planId: null,
  enterpriseName: '',
  unifiedSocialCreditCode: '',
  enterpriseAddress: '',
  industry: '',
  enterpriseScale: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  internshipPosition: '',
  internshipStartDate: '',
  internshipEndDate: '',
  applyReason: ''
})

const selectedSelfApplyPlan = ref(null)

const applyPostFormRules = {
  planId: [
    { required: true, message: '请选择实习计划', trigger: 'change' }
  ],
  internshipStartDate: [
    { required: true, message: '请选择实习开始日期', trigger: 'change' }
  ],
  internshipEndDate: [
    { required: true, message: '请选择实习结束日期', trigger: 'change' }
  ],
  resumeAttachment: [
    // 简历附件改为非必填，允许不上传
    // { 
    //   validator: (rule, value, callback) => {
    //     if (resumeAttachmentUrls.value.length === 0 && resumeFileList.value.length === 0) {
    //       callback(new Error('请上传简历附件'))
    //     } else {
    //       callback()
    //     }
    //   }, 
    //   trigger: 'change' 
    // }
  ],
  applyReason: [{ required: true, message: '请输入申请理由', trigger: 'blur' }]
}

const selfApplyFormRules = {
  planId: [
    { required: true, message: '请选择实习计划', trigger: 'change' }
  ],
  enterpriseName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  enterpriseAddress: [{ required: true, message: '请输入企业地址', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  internshipPosition: [{ required: true, message: '请输入实习岗位', trigger: 'blur' }],
  internshipStartDate: [{ required: true, message: '请选择实习开始日期', trigger: 'change' }],
  internshipEndDate: [{ required: true, message: '请选择实习结束日期', trigger: 'change' }],
  applyReason: [{ required: true, message: '请输入申请理由', trigger: 'blur' }]
}

// 加载岗位数据
const loadPostData = async () => {
  loading.value = true
  try {
    const res = await postApi.getPostPage({
      current: postPagination.current,
      size: postPagination.size,
      postName: searchForm.postName || undefined,
      enterpriseName: searchForm.enterpriseName || undefined,
      status: 3, // 只显示已发布的岗位
      cooperationOnly: true // 只显示合作企业的岗位
    })
    if (res.code === 200) {
      postTableData.value = res.data.records || []
      postPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载岗位数据失败:', error)
    ElMessage.error('加载岗位数据失败')
  } finally {
    loading.value = false
  }
}

// 加载合作企业申请数据
const loadCooperationApplyData = async () => {
  cooperationApplyLoading.value = true
  try {
    const res = await applyApi.getApplyPage({
      current: cooperationApplyPagination.current,
      size: cooperationApplyPagination.size,
      applyType: 1 // 合作企业申请
    })
    if (res.code === 200) {
      cooperationApplyTableData.value = res.data.records || []
      cooperationApplyPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载合作企业申请数据失败:', error)
    ElMessage.error('加载申请数据失败')
  } finally {
    cooperationApplyLoading.value = false
  }
}

// 加载申请数据（自主实习）
const loadApplyData = async () => {
  applyLoading.value = true
  try {
    const res = await applyApi.getApplyPage({
      current: applyPagination.current,
      size: applyPagination.size,
      applyType: 2 // 自主实习
    })
    if (res.code === 200) {
      applyTableData.value = res.data.records || []
      applyPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载申请数据失败:', error)
    ElMessage.error('加载申请数据失败')
  } finally {
    applyLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  postPagination.current = 1
  loadPostData()
}

// 重置
const handleReset = () => {
  searchForm.postName = ''
  searchForm.enterpriseName = ''
  handleSearch()
}

// Tab切换
const handleTabChange = (tabName) => {
  if (tabName === 'cooperation') {
    if (cooperationSubTab.value === 'postList') {
    loadPostData()
    } else if (cooperationSubTab.value === 'myApply') {
      loadCooperationApplyData()
    }
  } else if (tabName === 'self') {
    loadApplyData()
  }
}

// 合作企业申请子Tab切换
const handleCooperationSubTabChange = (tabName) => {
  if (tabName === 'postList') {
    loadPostData()
  } else if (tabName === 'myApply') {
    loadCooperationApplyData()
  }
}

// 查看岗位详情
const handleViewPost = async (row) => {
  try {
    const res = await postApi.getPostById(row.postId)
    if (res.code === 200) {
      postDetailData.value = res.data
      postDetailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询岗位详情失败:', error)
    ElMessage.error('查询岗位详情失败')
  }
}

// 申请岗位
// 新增：加载可用计划列表
const loadAvailablePlans = async () => {
  loadingPlans.value = true
  try {
    const res = await planApi.getAvailablePlans()
    if (res.code === 200) {
      availablePlans.value = res.data || []
      if (availablePlans.value.length === 0) {
        ElMessage.warning('当前没有可用的实习计划，请联系管理员')
      }
    } else {
      ElMessage.error(res.message || '加载实习计划失败')
    }
  } catch (error) {
    ElMessage.error('加载实习计划失败：' + (error.message || '未知错误'))
  } finally {
    loadingPlans.value = false
  }
}

// 新增：计划选择变化处理
const handlePlanChange = (planId) => {
  selectedPlan.value = availablePlans.value.find(p => p.planId === planId)
  if (selectedPlan.value) {
    // 自动填充实习时间
    applyPostForm.internshipStartDate = selectedPlan.value.startDate
    applyPostForm.internshipEndDate = selectedPlan.value.endDate
  } else {
    applyPostForm.internshipStartDate = null
    applyPostForm.internshipEndDate = null
  }
}

// 新增：禁用开始日期（不能早于计划开始日期，不能晚于计划结束日期）
const disabledStartDate = (date) => {
  if (!selectedPlan.value) return false
  const planStart = new Date(selectedPlan.value.startDate)
  const planEnd = new Date(selectedPlan.value.endDate)
  return date < planStart || date > planEnd
}

// 新增：禁用结束日期（不能早于开始日期，不能晚于计划结束日期）
const disabledEndDate = (date) => {
  if (!selectedPlan.value) return false
  const planStart = new Date(selectedPlan.value.startDate)
  const planEnd = new Date(selectedPlan.value.endDate)
  if (applyPostForm.internshipStartDate) {
    const startDate = new Date(applyPostForm.internshipStartDate)
    return date < startDate || date > planEnd
  }
  return date < planStart || date > planEnd
}

// 新增：自主实习申请计划选择变化处理
const handleSelfApplyPlanChange = (planId) => {
  selectedSelfApplyPlan.value = availablePlans.value.find(p => p.planId === planId)
  if (selectedSelfApplyPlan.value) {
    // 自动填充实习时间
    selfApplyForm.internshipStartDate = selectedSelfApplyPlan.value.startDate
    selfApplyForm.internshipEndDate = selectedSelfApplyPlan.value.endDate
  } else {
    selfApplyForm.internshipStartDate = ''
    selfApplyForm.internshipEndDate = ''
  }
}

// 新增：禁用自主实习开始日期
const disabledSelfApplyStartDate = (date) => {
  if (!selectedSelfApplyPlan.value) return false
  const planStart = new Date(selectedSelfApplyPlan.value.startDate)
  const planEnd = new Date(selectedSelfApplyPlan.value.endDate)
  return date < planStart || date > planEnd
}

// 新增：禁用自主实习结束日期
const disabledSelfApplyEndDate = (date) => {
  if (!selectedSelfApplyPlan.value) return false
  const planStart = new Date(selectedSelfApplyPlan.value.startDate)
  const planEnd = new Date(selectedSelfApplyPlan.value.endDate)
  if (selfApplyForm.internshipStartDate) {
    const startDate = new Date(selfApplyForm.internshipStartDate)
    return date < startDate || date > planEnd
  }
  return date < planStart || date > planEnd
}

const handleApplyPost = (row) => {
  applyPostForm.enterpriseId = row.enterpriseId
  applyPostForm.postId = row.postId
  applyPostForm.enterpriseName = row.enterpriseName || ''
  applyPostForm.postName = row.postName
  applyPostForm.planId = null
  applyPostForm.applyReason = ''
  applyPostForm.internshipStartDate = null
  applyPostForm.internshipEndDate = null
  selectedPlan.value = null
  resumeFileList.value = []
  resumeAttachmentUrls.value = []
  
  // 加载可用计划
  loadAvailablePlans()
  
  applyPostDialogVisible.value = true
}

// 从岗位详情申请
const handleApplyFromPostDetail = () => {
  postDetailDialogVisible.value = false
  applyPostForm.enterpriseId = postDetailData.value.enterpriseId
  applyPostForm.postId = postDetailData.value.postId
  applyPostForm.enterpriseName = postDetailData.value.enterpriseName || ''
  applyPostForm.postName = postDetailData.value.postName
  applyPostForm.planId = null
  applyPostForm.applyReason = ''
  applyPostForm.internshipStartDate = null
  applyPostForm.internshipEndDate = null
  selectedPlan.value = null
  resumeFileList.value = []
  resumeAttachmentUrls.value = []
  
  // 加载可用计划
  loadAvailablePlans()
  
  applyPostDialogVisible.value = true
}

// 简历文件上传前验证
const beforeResumeUpload = (file) => {
  const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
  const fileExtension = file.name.split('.').pop().toLowerCase()
  const allowedExtensions = ['pdf', 'doc', 'docx']
  
  if (!allowedExtensions.includes(fileExtension)) {
    ElMessage.error('只支持上传Word文档（.doc, .docx）和PDF文件（.pdf）')
    return false
  }
  
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  
  return true
}

// 简历文件变化
const handleResumeFileChange = (file, fileList) => {
  // 文件已添加到列表，稍后统一上传
}

// 简历文件移除
const handleResumeFileRemove = (file, fileList) => {
  // 文件已从列表移除
}

// 上传简历附件
const uploadResumeAttachments = async () => {
  if (resumeFileList.value.length === 0) {
    return resumeAttachmentUrls.value.join(',')
  }
  
  const files = resumeFileList.value.map(item => item.raw).filter(Boolean)
  if (files.length === 0) {
    return resumeAttachmentUrls.value.join(',')
  }
  
  try {
    const res = await fileApi.uploadFiles(files)
    if (res.code === 200 && res.data) {
      // 合并新上传的文件和已有附件
      const newUrls = [...resumeAttachmentUrls.value, ...res.data]
      return newUrls.join(',')
    }
  } catch (error) {
    console.error('简历附件上传失败:', error)
    ElMessage.error('简历附件上传失败: ' + (error.response?.data?.message || error.message))
    throw error
  }
  
  return resumeAttachmentUrls.value.join(',')
}

// 获取简历文件名
const getResumeFileName = (url) => {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1] || url
}

// 下载简历文件
const handleDownloadResume = async (filePath) => {
  if (!filePath) {
    ElMessage.warning('文件路径为空')
    return
  }
  try {
    await fileApi.downloadFile(filePath)
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败: ' + (error.message || '未知错误'))
  }
}

// 移除简历附件
const removeResumeAttachment = (index) => {
  resumeAttachmentUrls.value.splice(index, 1)
}

// 提交岗位申请
const handleSubmitPostApply = async () => {
  if (!applyPostFormRef.value) return
  await applyPostFormRef.value.validate(async (valid) => {
    if (valid) {
      applyPostLoading.value = true
      try {
        // 先上传简历附件
        let resumeAttachmentUrlsStr = ''
        try {
          resumeAttachmentUrlsStr = await uploadResumeAttachments()
        } catch (error) {
          // 附件上传失败，不阻止提交
          console.error('简历附件上传失败，继续提交:', error)
        }
        
        const res = await applyApi.addCooperationApply({
          planId: applyPostForm.planId,
          enterpriseId: applyPostForm.enterpriseId,
          postId: applyPostForm.postId,
          resumeContent: applyPostForm.resumeContent,
          resumeAttachment: resumeAttachmentUrlsStr,
          applyReason: applyPostForm.applyReason,
          internshipStartDate: applyPostForm.internshipStartDate,
          internshipEndDate: applyPostForm.internshipEndDate
        })
        if (res.code === 200) {
          ElMessage.success('申请提交成功')
          applyPostDialogVisible.value = false
          resumeFileList.value = []
          resumeAttachmentUrls.value = []
          loadCooperationApplyData() // 刷新合作企业申请列表
        }
      } catch (error) {
        console.error('提交申请失败:', error)
        ElMessage.error(error.response?.data?.message || '提交申请失败')
      } finally {
        applyPostLoading.value = false
      }
    }
  })
}

// 添加自主实习申请
const handleAddSelfApply = () => {
  selfApplyDialogTitle.value = '添加自主实习申请'
  resetSelfApplyForm()
  // 加载可用计划
  loadAvailablePlans()
  selfApplyDialogVisible.value = true
}

// 查看合作企业申请详情
const handleViewCooperationApply = async (row) => {
  try {
    const res = await applyApi.getApplyById(row.applyId)
    if (res.code === 200) {
      cooperationApplyDetailData.value = res.data
      cooperationApplyDetailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询申请详情失败:', error)
    ElMessage.error('查询申请详情失败')
  }
}

// 查看申请详情（自主实习）
const handleViewApply = async (row) => {
  try {
    const res = await applyApi.getApplyById(row.applyId)
    if (res.code === 200) {
      applyDetailData.value = res.data
      applyDetailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询申请详情失败:', error)
    ElMessage.error('查询申请详情失败')
  }
}

// 取消合作企业申请
const handleCancelCooperationApply = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该申请吗？', '提示', {
      type: 'warning'
    })
    const res = await applyApi.cancelApply(row.applyId)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      // 重置分页到第一页并刷新列表
      cooperationApplyPagination.current = 1
      await loadCooperationApplyData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败:', error)
      ElMessage.error(error.response?.data?.message || '取消失败')
    }
  }
}

// 确认上岗
const handleConfirmOnboard = async (row) => {
  if (!row.applyId) {
    ElMessage.error('申请ID不存在，无法确认上岗')
    return
  }
  
  try {
    await ElMessageBox.confirm('确认上岗后，您将开始在该企业实习，可以开始考勤。确定要确认上岗吗？', '确认上岗', {
      type: 'warning'
    })
    confirmOnboardLoading.value = row.applyId
    try {
      const res = await applyApi.confirmOnboard(row.applyId)
      if (res.code === 200) {
        ElMessage.success('确认上岗成功，可以开始实习了！')
        // 刷新列表
        await loadCooperationApplyData()
      }
    } catch (error) {
      console.error('确认上岗失败:', error)
      ElMessage.error(error.response?.data?.message || '确认上岗失败')
    } finally {
      confirmOnboardLoading.value = null
    }
  } catch (error) {
    // 用户取消
  }
}

// 取消申请（自主实习）
const handleCancelApply = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该申请吗？', '提示', {
      type: 'warning'
    })
    const res = await applyApi.cancelApply(row.applyId)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      // 重置分页到第一页并刷新列表
      applyPagination.current = 1
      await loadApplyData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败:', error)
      ElMessage.error(error.response?.data?.message || '取消失败')
    }
  }
}

// 提交自主实习申请
const handleSubmitSelfApply = async () => {
  if (!selfApplyFormRef.value) return
  await selfApplyFormRef.value.validate(async (valid) => {
    if (valid) {
      selfApplyLoading.value = true
      try {
        // 映射前端表单字段到后端实体字段
        const submitData = {
          planId: selfApplyForm.planId,
          selfEnterpriseName: selfApplyForm.enterpriseName,
          selfEnterpriseAddress: selfApplyForm.enterpriseAddress,
          selfContactPerson: selfApplyForm.contactPerson,
          selfContactPhone: selfApplyForm.contactPhone,
          selfEnterpriseNature: selfApplyForm.industry, // 行业对应企业性质
          selfPostName: selfApplyForm.internshipPosition,
          selfStartDate: selfApplyForm.internshipStartDate,
          selfEndDate: selfApplyForm.internshipEndDate,
          selfDescription: '', // 如果有实习说明字段，可以添加
          applyReason: selfApplyForm.applyReason
        }
        const res = await applyApi.addSelfApply(submitData)
        if (res.code === 200) {
          ElMessage.success('申请提交成功，等待审核')
          selfApplyDialogVisible.value = false
          loadApplyData()
        }
      } catch (error) {
        console.error('提交申请失败:', error)
        ElMessage.error(error.response?.data?.message || '提交申请失败')
      } finally {
        selfApplyLoading.value = false
      }
    }
  })
}

// 重置自主实习表单
const resetSelfApplyForm = () => {
  Object.assign(selfApplyForm, {
    applyId: null,
    planId: null,
    enterpriseName: '',
    unifiedSocialCreditCode: '',
    enterpriseAddress: '',
    industry: '',
    enterpriseScale: '',
    contactPerson: '',
    contactPhone: '',
    contactEmail: '',
    internshipPosition: '',
    internshipStartDate: '',
    internshipEndDate: '',
    applyReason: ''
  })
  selectedSelfApplyPlan.value = null
  if (selfApplyFormRef.value) {
    selfApplyFormRef.value.clearValidate()
  }
}

// 分页处理
const handlePostSizeChange = () => {
  loadPostData()
}

const handlePostPageChange = () => {
  loadPostData()
}

const handleCooperationApplySizeChange = () => {
  loadCooperationApplyData()
}

const handleCooperationApplyPageChange = () => {
  loadCooperationApplyData()
}

const handleApplySizeChange = () => {
  loadApplyData()
}

const handleApplyPageChange = () => {
  loadApplyData()
}

// 获取申请状态文本
const getApplyStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取申请状态类型（根据状态文本动态判断颜色）
const getApplyStatusType = (status, statusText) => {
  // 如果有状态文本，优先根据状态文本判断
  if (statusText) {
    if (statusText.includes('等待') || statusText.includes('待')) {
      return 'warning' // 黄色 - 等待状态
    }
    if (statusText.includes('通过') || statusText.includes('已确认') || statusText.includes('已录用')) {
      return 'success' // 绿色 - 成功/通过状态
    }
    if (statusText.includes('拒绝') || statusText.includes('未通过') || statusText.includes('已取消')) {
      return 'danger' // 红色 - 拒绝/失败状态
    }
    if (statusText.includes('已完成') || statusText.includes('待定')) {
      return 'info' // 灰色 - 信息状态
    }
  }
  
  // 如果没有状态文本，根据基础状态值判断
  const typeMap = {
    0: 'warning', // 待审核 - 黄色
    1: 'info',    // 已通过（等待企业处理）- 灰色，不是绿色
    2: 'danger',  // 已拒绝 - 红色
    3: 'success', // 已录用 - 绿色
    4: 'danger',  // 已拒绝录用 - 红色
    5: 'info'     // 已取消 - 灰色
  }
  return typeMap[status] || 'info'
}

// 初始化
onMounted(() => {
  loadPostData()
})
</script>

<style scoped>
.apply-section {
  padding: 20px 0;
}

.search-bar {
  margin-bottom: 20px;
}

.search-form {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

