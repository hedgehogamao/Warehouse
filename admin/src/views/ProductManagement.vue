<template>
  <div class="product-management">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item :label="t('common.keyword')">
          <el-input v-model="searchForm.keyword" :placeholder="`${t('product.name')}/SKU/${t('product.carModel')}`" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item :label="t('product.brand')">
          <el-input v-model="searchForm.brand" :placeholder="t('product.brand')" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item :label="t('product.category')">
          <el-select v-model="searchForm.category" :placeholder="t('common.all')" clearable @change="handleSearch">
            <el-option v-for="c in categoryList" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-select v-model="searchForm.status" :placeholder="t('common.all')" clearable @change="handleSearch">
            <el-option :label="t('product.listed')" :value="1" />
            <el-option :label="t('product.delisted')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('product.list') }}</span>
          <div>
            <el-button @click="handleDownloadTemplate">{{ t('product.downloadTemplate') }}</el-button>
            <el-button type="success" @click="importDialogVisible = true">{{ t('product.import') }}</el-button>
            <el-button type="primary" @click="openDialog('add')">{{ t('product.add') }}</el-button>
          </div>
        </div>
      </template>
      <el-table :data="productList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column :label="t('product.name')" min-width="180" show-overflow-tooltip>
          <template #default="{ row }"><span class="product-name">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="sku" label="SKU" width="130" />
        <el-table-column prop="brand" :label="t('product.brand')" width="90" />
        <el-table-column prop="category" :label="t('product.category')" width="80" />
        <el-table-column :label="t('product.stock')" width="100">
          <template #default="{ row }"><el-tag :type="getStockTagType(row)" size="small">{{ row.stock }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="t('product.salePrice')" width="100">
          <template #default="{ row }">¥{{ row.salePrice }}</template>
        </el-table-column>
        <el-table-column :label="t('common.status')" width="80">
          <template #default="{ row }"><el-switch :model-value="row.status === 1" @change="(val) => handleStatusChange(row, val)" /></template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="t('common.createdAt')" width="160" />
        <el-table-column :label="t('common.actions')" fixed="right" width="140">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog('edit', row)">{{ t('common.edit') }}</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total > 0" class="pagination" :current-page="page" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? t('product.add') : t('product.edit')" width="620px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item :label="t('product.name')" prop="name"><el-input v-model="dialogForm.name" :placeholder="t('product.namePlaceholder')" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item :label="t('product.sku')" prop="sku"><el-input v-model="dialogForm.sku" :disabled="dialogType === 'edit'" :placeholder="t('product.skuPlaceholder')" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item :label="t('product.brand')"><el-input v-model="dialogForm.brand" :placeholder="t('product.brandPlaceholder')" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item :label="t('product.category')"><el-select v-model="dialogForm.category" :placeholder="t('product.categoryPlaceholder')" filterable allow-create style="width: 100%"><el-option v-for="c in categoryList" :key="c" :label="c" :value="c" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item :label="t('product.carModel')"><el-input v-model="dialogForm.carModel" :placeholder="t('product.carModelPlaceholder')" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item :label="t('product.unit')" prop="unit"><el-input v-model="dialogForm.unit" :placeholder="t('product.unitPlaceholder')" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item :label="t('product.costPrice')"><el-input-number v-model="dialogForm.costPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item :label="t('product.salePrice')" prop="salePrice"><el-input-number v-model="dialogForm.salePrice" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item :label="t('product.minStock')"><el-input-number v-model="dialogForm.minStock" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item :label="t('product.imageUrl')">
              <el-upload
                class="image-uploader"
                :action="'/api/v1/upload/image'"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
                :before-upload="beforeUpload"
              >
                <img v-if="dialogForm.imageUrl" :src="dialogForm.imageUrl" class="uploaded-image" />
                <el-icon v-else class="upload-placeholder"><Plus /></el-icon>
              </el-upload>
              <el-input v-model="dialogForm.imageUrl" :placeholder="t('product.imageUrlPlaceholder')" style="margin-top: 8px;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('product.description')"><el-input v-model="dialogForm.description" type="textarea" :rows="3" :placeholder="t('product.descriptionPlaceholder')" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="handleDialogSubmit">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- Excel 导入弹窗 -->
    <el-dialog v-model="importDialogVisible" :title="t('product.importTitle')" width="520px" destroy-on-close>
      <div class="import-body">
        <p class="import-tip">{{ t('product.importTip') }}</p>
        <el-upload
          ref="importUploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".xlsx"
          :on-change="handleImportFileChange"
          :on-exceed="() => ElMessage.warning('只能上传一个文件')"
          drag
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip><div class="el-upload__tip">仅支持 .xlsx 格式文件</div></template>
        </el-upload>

        <div v-if="importResult" class="import-result">
          <el-alert
            :title="t('product.importSuccess', { success: importResult.success, skipped: importResult.skipped })"
            type="success"
            :closable="false"
            show-icon
          />
          <div v-if="importResult.errors && importResult.errors.length > 0" class="import-errors">
            <p><strong>{{ t('product.importErrors') }}:</strong></p>
            <el-table :data="importResult.errors" size="small" max-height="200" stripe>
              <el-table-column :label="t('product.row')" prop="row" width="60" />
              <el-table-column :label="t('product.reason')" prop="reason" />
            </el-table>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="importLoading" :disabled="!importFile" @click="handleImport">{{ t('product.importExcel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'
import { getProductList, createProduct, updateProduct, deleteProduct, updateProductStatus, getCategories, importProducts, downloadImportTemplate } from '@/api/product'

const { t } = useI18n()

// 上传相关
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}))

function beforeUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('仅支持 jpg/png/gif/webp 格式')
  if (!isLt5M) ElMessage.error('文件大小不能超过 5MB')
  return isImage && isLt5M
}

function handleUploadSuccess(response) {
  if (response.code === 200) {
    dialogForm.imageUrl = response.data.url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}
const loading = ref(false)
const productList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const categoryList = ref([])
const searchForm = reactive({ keyword: '', brand: '', category: '', status: undefined })

async function loadProducts() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.brand) params.brand = searchForm.brand
    if (searchForm.category) params.category = searchForm.category
    if (searchForm.status !== undefined && searchForm.status !== '') params.status = searchForm.status
    const res = await getProductList(params)
    productList.value = res.data.records
    total.value = Number(res.data.total) || 0
  } catch (e) {} finally { loading.value = false }
}

async function loadCategories() { try { const res = await getCategories(); categoryList.value = res.data || [] } catch (e) {} }
function handleSearch() { page.value = 1; loadProducts() }
function handleReset() { searchForm.keyword = ''; searchForm.brand = ''; searchForm.category = ''; searchForm.status = undefined; page.value = 1; loadProducts() }
function handlePageChange(val) { page.value = val; loadProducts() }
function getStockTagType(row) { if (row.stock <= 0) return 'danger'; if (row.minStock && row.stock <= row.minStock) return 'warning'; return 'success' }

async function handleStatusChange(row, val) {
  try { await updateProductStatus(row.id, val ? 1 : 0); ElMessage.success(val ? t('product.listedSuccess') : t('product.delistedSuccess')); loadProducts() } catch (e) { loadProducts() }
}

async function handleDelete(row) {
  try { await ElMessageBox.confirm(t('common.confirmDelete', { name: row.name }), t('common.prompt'), { type: 'warning' }); await deleteProduct(row.id); ElMessage.success(t('common.deleteSuccess')); loadProducts() } catch (e) { if (e !== 'cancel') {} }
}

const dialogVisible = ref(false)
const dialogType = ref('add')
const dialogLoading = ref(false)
const dialogFormRef = ref(null)
const editingId = ref(null)
const dialogForm = reactive({ name: '', sku: '', brand: '', carModel: '', category: '', unit: '个', costPrice: undefined, salePrice: undefined, minStock: undefined, imageUrl: '', description: '' })
const dialogRules = {
  name: [{ required: true, message: () => t('product.nameRequired'), trigger: 'blur' }],
  sku: [{ required: true, message: () => t('product.skuRequired'), trigger: 'blur' }],
  unit: [{ required: true, message: () => t('product.unitRequired'), trigger: 'blur' }],
  salePrice: [{ required: true, message: () => t('product.salePriceRequired'), trigger: 'blur' }]
}

function openDialog(type, row) {
  dialogType.value = type; dialogVisible.value = true
  if (type === 'edit' && row) {
    editingId.value = row.id
    Object.assign(dialogForm, { name: row.name || '', sku: row.sku || '', brand: row.brand || '', carModel: row.carModel || '', category: row.category || '', unit: row.unit || '个', costPrice: row.costPrice, salePrice: row.salePrice, minStock: row.minStock, imageUrl: row.imageUrl || '', description: row.description || '' })
  } else {
    editingId.value = null
    Object.assign(dialogForm, { name: '', sku: '', brand: '', carModel: '', category: '', unit: '个', costPrice: undefined, salePrice: undefined, minStock: undefined, imageUrl: '', description: '' })
  }
}

async function handleDialogSubmit() {
  const valid = await dialogFormRef.value.validate().catch(() => false)
  if (!valid) return
  const data = { name: dialogForm.name, brand: dialogForm.brand || undefined, carModel: dialogForm.carModel || undefined, category: dialogForm.category || undefined, unit: dialogForm.unit, costPrice: dialogForm.costPrice, salePrice: dialogForm.salePrice, minStock: dialogForm.minStock, imageUrl: dialogForm.imageUrl || undefined, description: dialogForm.description || undefined }
  dialogLoading.value = true
  try {
    if (dialogType.value === 'add') { data.sku = dialogForm.sku; await createProduct(data); ElMessage.success(t('common.addSuccess')) }
    else { await updateProduct(editingId.value, data); ElMessage.success(t('common.updateSuccess')) }
    dialogVisible.value = false; loadProducts(); loadCategories()
  } catch (e) {} finally { dialogLoading.value = false }
}

onMounted(() => { loadProducts(); loadCategories() })

// ===== Excel 导入 =====
const importDialogVisible = ref(false)
const importFile = ref(null)
const importLoading = ref(false)
const importResult = ref(null)
const importUploadRef = ref(null)

function handleImportFileChange(file) {
  importFile.value = file.raw
  importResult.value = null
}

async function handleImport() {
  if (!importFile.value) return
  importLoading.value = true
  try {
    const res = await importProducts(importFile.value)
    importResult.value = res.data
    ElMessage.success(t('product.importSuccess', { success: res.data.success, skipped: res.data.skipped }))
    loadProducts()
    loadCategories()
  } catch (e) {
    console.error('Import error:', e)
    const errorMsg = e.response?.data?.message || e.message || t('product.importFailed')
    ElMessage.error(errorMsg)
  } finally {
    importLoading.value = false
  }
}

async function handleDownloadTemplate() {
  try {
    const res = await downloadImportTemplate()
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.download = '商品导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('下载失败')
  }
}
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.product-name { font-weight: 500; }
.image-uploader :deep(.el-upload) {
  border: 1px dashed #e6dfd8;
  border-radius: 8px;
  cursor: pointer;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: border-color 0.2s;
}
.image-uploader :deep(.el-upload:hover) {
  border-color: #cc785c;
}
.uploaded-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
}
.upload-placeholder {
  font-size: 28px;
  color: #8e8b82;
}
.import-body {
  text-align: center;
}
.import-tip {
  color: #8e8b82;
  font-size: 13px;
  margin-bottom: 16px;
}
.import-result {
  margin-top: 16px;
  text-align: left;
}
.import-errors {
  margin-top: 12px;
}
</style>
