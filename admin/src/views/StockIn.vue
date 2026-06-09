<template>
  <div class="stock-in-management">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item :label="t('stockIn.product')">
          <el-select v-model="searchProductId" :placeholder="t('stockIn.selectProduct')" filterable remote clearable :remote-method="searchProducts" :loading="productSearching" @change="handleSearch" @clear="handleSearch" style="width: 260px">
            <el-option v-for="p in productOptions" :key="p.id" :label="`${p.name} (${p.sku})`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('stockIn.date')">
          <el-date-picker v-model="dateRange" type="daterange" :range-separator="t('stockIn.rangeSeparator')" :start-placeholder="t('stockIn.startDate')" :end-placeholder="t('stockIn.endDate')" value-format="YYYY-MM-DD" @change="handleSearch" />
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
          <span>{{ t('stockIn.list') }}</span>
          <el-button type="primary" @click="openStockInDialog">{{ t('stockIn.add') }}</el-button>
        </div>
      </template>
      <el-table :data="recordList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column :label="t('product.imageUrl')" width="80">
          <template #default="{ row }">
            <el-image v-if="row.imageUrl" :src="row.imageUrl" :preview-src-list="[row.imageUrl]" fit="cover" style="width: 48px; height: 48px; border-radius: 6px;" />
            <span v-else style="color: #8e8b82; font-size: 12px;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="t('stockIn.time')" width="160" />
        <el-table-column prop="productName" :label="t('stockIn.productName')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="productSku" :label="t('stockIn.sku')" width="130" />
        <el-table-column prop="quantity" :label="t('common.quantity')" width="80" />
        <el-table-column :label="t('stockIn.costPrice')" width="100">
          <template #default="{ row }">{{ row.costPrice != null ? `¥${row.costPrice}` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="operatorName" :label="t('common.operator')" width="90" />
        <el-table-column prop="remark" :label="t('common.remark')" min-width="140" show-overflow-tooltip />
      </el-table>
      <el-pagination v-if="total > 0" class="pagination" :current-page="page" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="t('stockIn.add')" width="500px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="90px">
        <el-form-item :label="t('stockIn.product')" prop="productId">
          <el-select v-model="dialogForm.productId" :placeholder="t('stockIn.searchProduct')" filterable remote :remote-method="searchProductsForDialog" :loading="productSearching" style="width: 100%">
            <el-option v-for="p in dialogProductOptions" :key="p.id" :label="`${p.name} (${p.sku})`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('stockIn.quantityLabel')" prop="quantity">
          <el-input-number v-model="dialogForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="t('stockIn.costPrice')">
          <el-input-number v-model="dialogForm.costPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="t('common.remark')">
          <el-input v-model="dialogForm.remark" type="textarea" :rows="2" :placeholder="t('stockIn.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="handleDialogSubmit">{{ t('stockIn.confirmBtn') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getStockInList, createStockIn } from '@/api/stock-in'
import { getProductList } from '@/api/product'

const { t } = useI18n()
const loading = ref(false)
const recordList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const searchProductId = ref(null)
const dateRange = ref(null)

async function loadRecords() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (searchProductId.value) params.productId = searchProductId.value
    if (dateRange.value && dateRange.value.length === 2) { params.startDate = dateRange.value[0]; params.endDate = dateRange.value[1] }
    const res = await getStockInList(params)
    recordList.value = res.data.records; total.value = Number(res.data.total) || 0
  } catch (e) {} finally { loading.value = false }
}

function handleSearch() { page.value = 1; loadRecords() }
function handleReset() { searchProductId.value = null; dateRange.value = null; page.value = 1; loadRecords() }
function handlePageChange(val) { page.value = val; loadRecords() }

const productOptions = ref([])
const dialogProductOptions = ref([])
const productSearching = ref(false)

async function searchProducts(query) {
  if (!query || query.length < 1) return
  productSearching.value = true
  try { const res = await getProductList({ page: 1, size: 20, keyword: query }); productOptions.value = res.data.records } catch (e) {} finally { productSearching.value = false }
}
async function searchProductsForDialog(query) {
  if (!query || query.length < 1) return
  productSearching.value = true
  try { const res = await getProductList({ page: 1, size: 20, keyword: query }); dialogProductOptions.value = res.data.records } catch (e) {} finally { productSearching.value = false }
}

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogFormRef = ref(null)
const dialogForm = reactive({ productId: null, quantity: 1, costPrice: undefined, remark: '' })
const dialogRules = {
  productId: [{ required: true, message: () => t('stockIn.productRequired'), trigger: 'change' }],
  quantity: [{ required: true, message: () => t('stockIn.quantityRequired'), trigger: 'blur' }]
}

function openStockInDialog() {
  Object.assign(dialogForm, { productId: null, quantity: 1, costPrice: undefined, remark: '' })
  dialogProductOptions.value = []; dialogVisible.value = true
}

async function handleDialogSubmit() {
  const valid = await dialogFormRef.value.validate().catch(() => false)
  if (!valid) return
  dialogLoading.value = true
  try {
    const data = { productId: dialogForm.productId, quantity: dialogForm.quantity, remark: dialogForm.remark || undefined }
    if (dialogForm.costPrice != null) data.costPrice = dialogForm.costPrice
    const res = await createStockIn(data)
    const d = res.data
    ElMessage.success(t('stockIn.successMsg', { name: d.productName, before: d.beforeStock, after: d.afterStock }))
    dialogVisible.value = false; loadRecords()
  } catch (e) {} finally { dialogLoading.value = false }
}

onMounted(() => { loadRecords() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
