<template>
  <div class="statistics-page">
    <!-- 时间范围选择 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-radio-group v-model="days" @change="loadAll">
        <el-radio-button :value="7">7{{ t('statistics.days') }}</el-radio-button>
        <el-radio-button :value="30">30{{ t('statistics.days') }}</el-radio-button>
        <el-radio-button :value="90">90{{ t('statistics.days') }}</el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- 销售趋势 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <template #header><span class="card-title">{{ t('dashboard.salesTrend') }}</span></template>
      <div ref="trendChartRef" style="height: 350px;"></div>
    </el-card>

    <el-row :gutter="16">
      <!-- 商品排行 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span class="card-title">{{ t('dashboard.topProducts') }}</span></template>
          <el-table :data="topProducts" stripe size="small">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="productName" :label="t('product.name')" />
            <el-table-column prop="category" :label="t('product.category')" width="100" />
            <el-table-column :label="t('common.quantity')" width="100"><template #default="{ row }">{{ row.totalQuantity }}</template></el-table-column>
            <el-table-column :label="t('common.amount')" width="120"><template #default="{ row }"><span class="amount">C${{ row.totalAmount }}</span></template></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <!-- 分类占比 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span class="card-title">{{ t('dashboard.categoryDistribution') }}</span></template>
          <div ref="pieChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts'
import { getSalesTrend, getTopProducts, getCategoryDistribution } from '@/api/statistics'

const { t } = useI18n()
const days = ref(7)
const topProducts = ref([])
const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

async function loadAll() {
  try {
    const [trendRes, topRes, distRes] = await Promise.all([
      getSalesTrend(days.value),
      getTopProducts(days.value, 10),
      getCategoryDistribution(days.value)
    ])
    topProducts.value = topRes.data
    await nextTick()
    renderTrend(trendRes.data)
    renderPie(distRes.data)
  } catch (e) {}
}

function renderTrend(data) {
  if (!trendChartRef.value) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.days, axisLine: { lineStyle: { color: '#e6dfd8' } }, axisLabel: { color: '#6c6a64' } },
    yAxis: [
      { type: 'value', name: 'C$', splitLine: { lineStyle: { color: '#ebe6df' } }, axisLabel: { color: '#6c6a64' } },
      { type: 'value', name: t('dashboard.orders'), splitLine: { show: false }, axisLabel: { color: '#6c6a64' } }
    ],
    series: [
      { name: t('common.amount'), type: 'line', data: data.amounts, smooth: true, lineStyle: { color: '#cc785c', width: 2 }, itemStyle: { color: '#cc785c' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(204,120,92,0.2)' }, { offset: 1, color: 'rgba(204,120,92,0)' }]) } },
      { name: t('dashboard.orders'), type: 'bar', yAxisIndex: 1, data: data.counts, barWidth: 16, itemStyle: { color: '#e8e0d2', borderRadius: [4, 4, 0, 0] } }
    ]
  })
}

function renderPie(data) {
  if (!pieChartRef.value) return
  if (pieChart) pieChart.dispose()
  pieChart = echarts.init(pieChartRef.value)
  const colors = ['#cc785c', '#5db8a6', '#e8a55a', '#d4a017', '#6c6a64', '#8e8b82']
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: C${c} ({d}%)' },
    series: [{ type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: false, itemStyle: { borderRadius: 6, borderColor: '#faf9f5', borderWidth: 2 }, label: { show: true, formatter: '{b}\n{d}%', color: '#3d3d3a' }, data: data.map((d, i) => ({ value: d.amount, name: d.category, itemStyle: { color: colors[i % colors.length] } })) }]
  })
}

function handleResize() { trendChart?.resize(); pieChart?.resize() }

onMounted(() => { loadAll(); window.addEventListener('resize', handleResize) })
onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); trendChart?.dispose(); pieChart?.dispose() })
</script>

<style scoped>
.card-title { font-family: var(--claude-font-display); font-size: 16px; font-weight: 500; }
.amount { font-family: var(--claude-font-display); font-weight: 600; color: #cc785c; }
</style>
