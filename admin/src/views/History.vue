<template>
  <div class="history-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- 库存流水 Tab -->
        <el-tab-pane :label="t('history.inventoryLogs')" name="inventory">
          <el-form :inline="true" @submit.prevent="loadInventoryLogs" style="margin-bottom: 12px;">
            <el-form-item :label="t('common.status')">
              <el-select v-model="invFilters.type" :placeholder="t('common.all')" clearable @change="loadInventoryLogs" style="width: 120px;">
                <el-option :label="t('inventory.typeStockIn')" value="STOCK_IN" />
                <el-option :label="t('inventory.typeSale')" value="SALE" />
                <el-option :label="t('inventory.typeAdjust')" value="ADJUST" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('product.name')">
              <el-input v-model="invFilters.productName" :placeholder="t('product.name')" clearable @clear="loadInventoryLogs" style="width: 160px;" />
            </el-form-item>
            <el-form-item :label="t('order.date')">
              <el-date-picker v-model="invFilters.dateRange" type="daterange" :range-separator="t('stockIn.rangeSeparator')" :start-placeholder="t('stockIn.startDate')" :end-placeholder="t('stockIn.endDate')" value-format="YYYY-MM-DD" @change="loadInventoryLogs" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadInventoryLogs">{{ t('common.search') }}</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="invLogs" v-loading="invLoading" stripe size="small">
            <el-table-column prop="createdAt" :label="t('order.orderTime')" width="160" />
            <el-table-column :label="t('inventory.typeStockIn')" width="80">
              <template #default="{ row }"><el-tag :type="getLogTypeTag(row.type)" size="small">{{ getLogTypeName(row.type) }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="quantity" :label="t('inventory.change')" width="70">
              <template #default="{ row }"><span :style="{ color: row.quantity > 0 ? '#5db872' : '#c64545' }">{{ row.quantity > 0 ? '+' : '' }}{{ row.quantity }}</span></template>
            </el-table-column>
            <el-table-column prop="beforeStock" :label="t('inventory.before')" width="70" />
            <el-table-column prop="afterStock" :label="t('inventory.after')" width="70" />
            <el-table-column prop="remark" :label="t('common.remark')" min-width="200" show-overflow-tooltip />
          </el-table>
          <el-pagination v-if="invTotal > 0" class="pagination" :current-page="invPage" :page-size="invPageSize" :total="invTotal" layout="total, prev, pager, next" size="small" @current-change="(v) => { invPage = v; loadInventoryLogs() }" />
        </el-tab-pane>

        <!-- 操作日志 Tab -->
        <el-tab-pane :label="t('history.operationLogs')" name="operation">
          <el-form :inline="true" @submit.prevent="loadOperationLogs" style="margin-bottom: 12px;">
            <el-form-item :label="t('history.module')">
              <el-select v-model="opFilters.module" :placeholder="t('common.all')" clearable @change="loadOperationLogs" style="width: 130px;">
                <el-option label="AUTH" value="AUTH" />
                <el-option label="PRODUCT" value="PRODUCT" />
                <el-option label="STOCK_IN" value="STOCK_IN" />
                <el-option label="ORDER" value="ORDER" />
                <el-option label="INVENTORY" value="INVENTORY" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('order.date')">
              <el-date-picker v-model="opFilters.dateRange" type="daterange" :range-separator="t('stockIn.rangeSeparator')" :start-placeholder="t('stockIn.startDate')" :end-placeholder="t('stockIn.endDate')" value-format="YYYY-MM-DD" @change="loadOperationLogs" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadOperationLogs">{{ t('common.search') }}</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="opLogs" v-loading="opLoading" stripe size="small">
            <el-table-column prop="createdAt" :label="t('order.orderTime')" width="160" />
            <el-table-column prop="module" :label="t('history.module')" width="100" />
            <el-table-column prop="action" :label="t('common.actions')" width="130" />
            <el-table-column prop="content" :label="t('history.content')" min-width="200" show-overflow-tooltip />
            <el-table-column prop="operatorName" :label="t('common.operator')" width="100" />
          </el-table>
          <el-pagination v-if="opTotal > 0" class="pagination" :current-page="opPage" :page-size="opPageSize" :total="opTotal" layout="total, prev, pager, next" size="small" @current-change="(v) => { opPage = v; loadOperationLogs() }" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getInventoryLogs, getOperationLogs } from '@/api/history'

const { t } = useI18n()
const activeTab = ref('inventory')

// --- 库存流水 ---
const invLoading = ref(false)
const invLogs = ref([])
const invTotal = ref(0)
const invPage = ref(1)
const invPageSize = ref(20)
const invFilters = reactive({ type: '', productName: '', dateRange: null })

async function loadInventoryLogs() {
  invLoading.value = true
  try {
    const params = { page: invPage.value, size: invPageSize.value }
    if (invFilters.type) params.type = invFilters.type
    if (invFilters.productName) params.productName = invFilters.productName
    if (invFilters.dateRange && invFilters.dateRange.length === 2) { params.startDate = invFilters.dateRange[0]; params.endDate = invFilters.dateRange[1] }
    const res = await getInventoryLogs(params)
    invLogs.value = res.data.records; invTotal.value = Number(res.data.total) || 0
  } catch (e) {} finally { invLoading.value = false }
}

function getLogTypeTag(type) { if (type === 'STOCK_IN') return 'success'; if (type === 'SALE') return 'danger'; return 'warning' }
function getLogTypeName(type) { if (type === 'STOCK_IN') return t('inventory.typeStockIn'); if (type === 'SALE') return t('inventory.typeSale'); if (type === 'ADJUST') return t('inventory.typeAdjust'); return type }

// --- 操作日志 ---
const opLoading = ref(false)
const opLogs = ref([])
const opTotal = ref(0)
const opPage = ref(1)
const opPageSize = ref(20)
const opFilters = reactive({ module: '', dateRange: null })

async function loadOperationLogs() {
  opLoading.value = true
  try {
    const params = { page: opPage.value, size: opPageSize.value }
    if (opFilters.module) params.module = opFilters.module
    if (opFilters.dateRange && opFilters.dateRange.length === 2) { params.startDate = opFilters.dateRange[0]; params.endDate = opFilters.dateRange[1] }
    const res = await getOperationLogs(params)
    opLogs.value = res.data.records; opTotal.value = Number(res.data.total) || 0
  } catch (e) {} finally { opLoading.value = false }
}

onMounted(() => { loadInventoryLogs(); loadOperationLogs() })
</script>

<style scoped>
.pagination { margin-top: 12px; justify-content: flex-end; }
</style>
