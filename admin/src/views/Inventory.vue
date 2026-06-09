<template>
  <div class="inventory-management">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item :label="t('common.keyword')">
          <el-input v-model="searchKeyword" :placeholder="`${t('product.name')}/SKU/${t('product.carModel')}`" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item :label="t('product.category')">
          <el-select v-model="searchCategory" :placeholder="t('common.all')" clearable @change="handleSearch">
            <el-option v-for="c in categoryList" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item><el-checkbox v-model="onlyLowStock" @change="handleSearch">{{ t('inventory.lowStockOnly') }}</el-checkbox></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('inventory.list') }}</span>
          <el-tag v-if="lowStockCount > 0" type="warning" size="small">{{ t('inventory.lowStockCount', { count: lowStockCount }) }}</el-tag>
        </div>
      </template>
      <el-table :data="inventoryList" v-loading="loading" stripe>
        <el-table-column prop="productName" :label="t('product.name')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sku" label="SKU" width="130" />
        <el-table-column prop="brand" :label="t('product.brand')" width="90" />
        <el-table-column prop="category" :label="t('product.category')" width="80" />
        <el-table-column :label="t('inventory.currentStock')" width="100">
          <template #default="{ row }"><el-tag :type="getStockTagType(row.status)" size="small">{{ row.stock }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="minStock" :label="t('inventory.alertThreshold')" width="80" />
        <el-table-column :label="t('common.status')" width="100">
          <template #default="{ row }"><el-tag :type="getStockTagType(row.status)" size="small" effect="dark">{{ getStockStatusLabel(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openLogDialog(row)">{{ t('inventory.log') }}</el-button>
            <el-button link type="warning" size="small" @click="openAdjustDialog(row)">{{ t('inventory.adjust') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total > 0" class="pagination" :current-page="page" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
    </el-card>

    <el-dialog v-model="logDialogVisible" :title="t('inventory.logTitle')" width="700px">
      <div v-if="logProduct" style="margin-bottom: 12px; color: #606266;">{{ logProduct.productName }}（{{ logProduct.sku }}）— {{ t('inventory.currentStock') }}: {{ logProduct.stock }}</div>
      <el-table :data="logList" v-loading="logLoading" stripe size="small">
        <el-table-column prop="createdAt" :label="t('inventory.change')" width="160" />
        <el-table-column :label="t('common.status')" width="80">
          <template #default="{ row }"><el-tag :type="getLogTypeTag(row.type)" size="small">{{ getLogTypeName(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="quantity" :label="t('inventory.change')" width="70">
          <template #default="{ row }"><span :style="{ color: row.quantity > 0 ? '#67c23a' : '#f56c6c' }">{{ row.quantity > 0 ? '+' : '' }}{{ row.quantity }}</span></template>
        </el-table-column>
        <el-table-column prop="beforeStock" :label="t('inventory.before')" width="70" />
        <el-table-column prop="afterStock" :label="t('inventory.after')" width="70" />
        <el-table-column prop="remark" :label="t('common.remark')" min-width="160" show-overflow-tooltip />
      </el-table>
      <el-pagination v-if="logTotal > 0" class="pagination" :current-page="logPage" :page-size="logPageSize" :total="logTotal" layout="total, prev, pager, next" size="small" @current-change="handleLogPageChange" />
    </el-dialog>

    <el-dialog v-model="adjustDialogVisible" :title="t('inventory.adjustTitle')" width="440px" destroy-on-close>
      <div v-if="adjustProduct" style="margin-bottom: 16px;">
        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item :label="t('stockIn.product')">{{ adjustProduct.productName }}</el-descriptions-item>
          <el-descriptions-item :label="t('inventory.currentStock')">{{ adjustProduct.stock }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="80px">
        <el-form-item :label="t('inventory.adjustQuantity')" prop="quantity">
          <el-input-number v-model="adjustForm.quantity" style="width: 100%" />
          <div class="form-tip">{{ t('inventory.adjustTip') }}</div>
        </el-form-item>
        <el-form-item :label="t('inventory.adjustReason')" prop="reason">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="2" :placeholder="t('inventory.reasonPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="adjustLoading" @click="handleAdjustSubmit">{{ t('inventory.confirmAdjust') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getInventoryList, getInventoryLogs, adjustStock } from '@/api/inventory'
import { getCategories } from '@/api/product'

const { t } = useI18n()
const loading = ref(false)
const inventoryList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const categoryList = ref([])
const lowStockCount = ref(0)
const searchKeyword = ref('')
const searchCategory = ref('')
const onlyLowStock = ref(false)

async function loadInventory() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchCategory.value) params.category = searchCategory.value
    if (onlyLowStock.value) params.lowStock = true
    const res = await getInventoryList(params)
    inventoryList.value = res.data.records; total.value = Number(res.data.total) || 0
  } catch (e) {} finally { loading.value = false }
}

async function loadLowStockCount() { try { const res = await getInventoryList({ page: 1, size: 1, lowStock: true }); lowStockCount.value = res.data.total } catch (e) {} }
async function loadCategories() { try { const res = await getCategories(); categoryList.value = res.data || [] } catch (e) {} }
function handleSearch() { page.value = 1; loadInventory() }
function handleReset() { searchKeyword.value = ''; searchCategory.value = ''; onlyLowStock.value = false; page.value = 1; loadInventory() }
function handlePageChange(val) { page.value = val; loadInventory() }
function getStockTagType(status) { if (status === 'OUT_OF_STOCK') return 'danger'; if (status === 'LOW_STOCK') return 'warning'; return 'success' }
function getStockStatusLabel(status) { if (status === 'OUT_OF_STOCK') return t('inventory.outOfStock'); if (status === 'LOW_STOCK') return t('inventory.lowStock'); return t('inventory.normal') }

const logDialogVisible = ref(false)
const logLoading = ref(false)
const logProduct = ref(null)
const logList = ref([])
const logTotal = ref(0)
const logPage = ref(1)
const logPageSize = ref(20)

async function openLogDialog(row) { logProduct.value = row; logPage.value = 1; logDialogVisible.value = true; await loadLogs() }
async function loadLogs() { logLoading.value = true; try { const res = await getInventoryLogs(logProduct.value.productId, { page: logPage.value, size: logPageSize.value }); logList.value = res.data.records; logTotal.value = Number(res.data.total) || 0 } catch (e) {} finally { logLoading.value = false } }
function handleLogPageChange(val) { logPage.value = val; loadLogs() }
function getLogTypeTag(type) { if (type === 'STOCK_IN') return 'success'; if (type === 'SALE') return 'danger'; return 'warning' }
function getLogTypeName(type) { if (type === 'STOCK_IN') return t('inventory.typeStockIn'); if (type === 'SALE') return t('inventory.typeSale'); if (type === 'ADJUST') return t('inventory.typeAdjust'); return type }

const adjustDialogVisible = ref(false)
const adjustLoading = ref(false)
const adjustProduct = ref(null)
const adjustFormRef = ref(null)
const adjustForm = reactive({ quantity: 0, reason: '' })
const adjustRules = {
  quantity: [{ required: true, message: () => t('inventory.quantityRequired'), trigger: 'blur' }],
  reason: [{ required: true, message: () => t('inventory.reasonRequired'), trigger: 'blur' }]
}

function openAdjustDialog(row) { adjustProduct.value = row; adjustForm.quantity = 0; adjustForm.reason = ''; adjustDialogVisible.value = true }

async function handleAdjustSubmit() {
  const valid = await adjustFormRef.value.validate().catch(() => false)
  if (!valid) return
  adjustLoading.value = true
  try { await adjustStock(adjustProduct.value.productId, { quantity: adjustForm.quantity, reason: adjustForm.reason }); ElMessage.success(t('inventory.adjustSuccess')); adjustDialogVisible.value = false; loadInventory(); loadLowStockCount() } catch (e) {} finally { adjustLoading.value = false }
}

onMounted(() => { loadInventory(); loadLowStockCount(); loadCategories() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.form-tip { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
