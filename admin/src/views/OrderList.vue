<template>
  <div class="order-management">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item :label="t('order.orderNo')"><el-input v-model="searchForm.orderNo" :placeholder="t('order.orderNoPlaceholder')" clearable @clear="handleSearch" /></el-form-item>
        <el-form-item :label="t('order.customer')"><el-input v-model="searchForm.customerName" :placeholder="t('order.customerNamePlaceholder')" clearable @clear="handleSearch" /></el-form-item>
        <el-form-item :label="t('order.date')">
          <el-date-picker v-model="dateRange" type="daterange" :range-separator="t('stockIn.rangeSeparator')" :start-placeholder="t('stockIn.startDate')" :end-placeholder="t('stockIn.endDate')" value-format="YYYY-MM-DD" @change="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header><div class="card-header"><span>{{ t('order.list') }}</span></div></template>
      <el-table :data="orderList" v-loading="loading" stripe>
        <el-table-column prop="orderNo" :label="t('order.orderNo')" width="170" />
        <el-table-column :label="t('order.customer')" width="100"><template #default="{ row }">{{ row.customerName || '-' }}</template></el-table-column>
        <el-table-column :label="t('common.amount')" width="110"><template #default="{ row }"><span class="amount">C${{ row.totalAmount }}</span></template></el-table-column>
        <el-table-column :label="t('order.paymentMethod')" width="100"><template #default="{ row }"><el-tag size="small" :type="getPaymentTagType(row.paymentMethod)">{{ row.paymentMethodName }}</el-tag></template></el-table-column>
        <el-table-column :label="t('common.status')" width="90"><template #default="{ row }"><el-tag :type="row.status === 'PAID' ? 'success' : 'info'" size="small">{{ row.status === 'PAID' ? t('order.paid') : t('order.refunded') }}</el-tag></template></el-table-column>
        <el-table-column prop="itemCount" :label="t('order.itemCount')" width="80" />
        <el-table-column prop="operatorName" :label="t('common.operator')" width="90" />
        <el-table-column prop="createdAt" :label="t('order.orderTime')" width="160" />
        <el-table-column :label="t('common.actions')" fixed="right" width="140">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">{{ t('order.detail') }}</el-button>
            <el-button v-if="row.status === 'PAID'" link type="danger" size="small" @click="handleRefund(row)">{{ t('order.refund') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total > 0" class="pagination" :current-page="page" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
    </el-card>

    <el-dialog v-model="detailVisible" :title="t('order.detailTitle')" width="640px">
      <div v-if="detail" class="order-detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item :label="t('order.orderNo')">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.status')"><el-tag :type="detail.status === 'PAID' ? 'success' : 'info'" size="small">{{ detail.status === 'PAID' ? t('order.paid') : t('order.refunded') }}</el-tag></el-descriptions-item>
          <el-descriptions-item :label="t('order.customer')">{{ detail.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="t('user.phone')">{{ detail.customerPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="t('order.paymentMethod')">{{ detail.paymentMethodName }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.operator')">{{ detail.operatorName }}</el-descriptions-item>
          <el-descriptions-item :label="t('order.orderTime')">{{ detail.createdAt }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.remark')">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px; font-family: var(--claude-font-display);">{{ t('order.itemDetails') }}</h4>
        <el-table :data="detail.items" stripe size="small" border>
          <el-table-column prop="productName" :label="t('product.name')" />
          <el-table-column prop="quantity" :label="t('common.quantity')" width="80" />
          <el-table-column :label="t('order.unitPrice')" width="100"><template #default="{ row }">C${{ row.unitPrice }}</template></el-table-column>
          <el-table-column :label="t('order.subtotal')" width="100"><template #default="{ row }">C${{ row.subtotal }}</template></el-table-column>
        </el-table>
        <div class="detail-total">{{ t('common.total') }}: <span class="total-amount">C${{ detail.totalAmount }}</span></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, getOrderById, refundOrder } from '@/api/order'

const { t } = useI18n()
const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const dateRange = ref(null)
const searchForm = reactive({ orderNo: '', customerName: '' })

async function loadOrders() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (searchForm.orderNo) params.orderNo = searchForm.orderNo
    if (searchForm.customerName) params.customerName = searchForm.customerName
    if (dateRange.value && dateRange.value.length === 2) { params.startDate = dateRange.value[0]; params.endDate = dateRange.value[1] }
    const res = await getOrderList(params)
    orderList.value = res.data.records; total.value = Number(res.data.total) || 0
  } catch (e) {} finally { loading.value = false }
}

function handleSearch() { page.value = 1; loadOrders() }
function handleReset() { searchForm.orderNo = ''; searchForm.customerName = ''; dateRange.value = null; page.value = 1; loadOrders() }
function handlePageChange(val) { page.value = val; loadOrders() }
function getPaymentTagType(method) { if (method === 'WECHAT') return 'success'; if (method === 'ALIPAY') return 'primary'; if (method === 'CASH') return 'warning'; return 'info' }

const detailVisible = ref(false)
const detail = ref(null)

async function openDetail(row) { try { const res = await getOrderById(row.id); detail.value = res.data; detailVisible.value = true } catch (e) {} }

async function handleRefund(row) {
  try {
    await ElMessageBox.confirm(t('order.refundConfirm', { orderNo: row.orderNo }), t('order.refundTitle'), { confirmButtonText: t('order.confirmRefund'), cancelButtonText: t('common.cancel'), type: 'warning' })
    await refundOrder(row.id, t('order.defaultReason'))
    ElMessage.success(t('order.refundSuccess'))
    loadOrders()
  } catch (e) { if (e !== 'cancel') {} }
}

onMounted(() => { loadOrders() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.amount { font-family: var(--claude-font-display); font-weight: 600; color: #cc785c; }
.detail-total { text-align: right; margin-top: 12px; font-size: 16px; font-weight: 600; color: #141413; }
.total-amount { font-family: var(--claude-font-display); font-size: 22px; color: #cc785c; }
</style>
