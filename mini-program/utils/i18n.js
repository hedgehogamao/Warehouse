/**
 * 小程序多语言模块
 * 支持中文(zh)和西班牙语(es)
 */

const messages = {
  zh: {
    // 通用
    'common.search': '搜索',
    'common.cancel': '取消',
    'common.confirm': '确认',
    'common.save': '保存',
    'common.delete': '删除',
    'common.total': '合计',
    'common.loading': '加载中...',
    'common.noMore': '没有更多了',
    'common.noData': '暂无数据',
    'common.stock': '现货',
    'common.lowStock': '库存不足',
    'common.outOfStock': '缺货',
    'common.brand': '品牌',
    'common.sku': '编码',
    'common.category': '分类',
    'common.carModel': '适配',
    'common.description': '商品描述',
    'common.addToCart': '购物车',
    'common.addCartBtn': '+ 购物车',
    'common.inStock': '有货',

    // Tab 栏
    'tab.home': '首页',
    'tab.products': '商品',
    'tab.stockout': '出库',
    'tab.cart': '购物车',
    'tab.mine': '我的',

    // 首页
    'index.searchPlaceholder': '搜索商品名称/SKU...',
    'index.categories': '商品分类',
    'index.hotProducts': '热门商品',

    // 商品列表
    'products.searchPlaceholder': '搜索商品...',

    // 商品详情
    'detail.stockInfo': '（库存 {count} 件）',
    'detail.goCart': '🛒 购物车',
    'detail.addCart': '加入购物车',
    'detail.outOfStock': '暂时缺货',

    // 购物车
    'cart.title': '购物车',
    'cart.itemCount': '共 {count} 件商品',
    'cart.clear': '清空',
    'cart.subtotal': '小计',
    'cart.tip1': '💡 请携带此清单到店结算',
    'cart.tip2': '到店后由店员为您创建订单并收款',
    'cart.contactStore': '📞 联系店家',
    'cart.emptyIcon': '🛒',
    'cart.emptyText': '购物车是空的',
    'cart.goShop': '去选购',
    'cart.confirmRemove': '确认移除此商品？',
    'cart.confirmClear': '确认清空购物车？',
    'cart.maxStock': '已达最大库存',
    'cart.added': '已加入购物车',

    // 我的
    'mine.storeName': '汽车配件商城',
    'mine.storeSub': '优质配件，到店选购',
    'mine.myInfo': '我的信息（选填）',
    'mine.name': '姓名',
    'mine.namePlaceholder': '方便店员称呼您',
    'mine.phone': '电话',
    'mine.phonePlaceholder': '方便联系您',
    'mine.saved': '已保存',
    'mine.callStore': '📞 电话联系店家',
    'mine.language': '语言 / Idioma',
    'mine.langZh': '中文',
    'mine.langEs': 'Español',

    // 出库
    'stockout.scanBtn': '扫码出库',
    'stockout.scanTip': '扫描商品条形码或手动输入 SKU',
    'stockout.manualInput': '手动输入',
    'stockout.skuPlaceholder': '输入 SKU 编码',
    'stockout.currentStock': '当前库存',
    'stockout.outQty': '出库数量',
    'stockout.remark': '备注',
    'stockout.remarkPlaceholder': '出库原因（选填）',
    'stockout.confirmOut': '确认出库',
    'stockout.noProduct': '未找到该商品',
    'stockout.outSuccess': '出库成功',
    'stockout.stockNotEnough': '库存不足'
  },

  es: {
    'common.search': 'Buscar',
    'common.cancel': 'Cancelar',
    'common.confirm': 'Confirmar',
    'common.save': 'Guardar',
    'common.delete': 'Eliminar',
    'common.total': 'Total',
    'common.loading': 'Cargando...',
    'common.noMore': 'No hay más',
    'common.noData': 'Sin datos',
    'common.stock': 'En stock',
    'common.lowStock': 'Stock bajo',
    'common.outOfStock': 'Sin stock',
    'common.brand': 'Marca',
    'common.sku': 'Código',
    'common.category': 'Categoría',
    'common.carModel': 'Compatibilidad',
    'common.description': 'Descripción',
    'common.addToCart': 'Carrito',
    'common.addCartBtn': '+ Carrito',
    'common.inStock': 'Disponible',

    'tab.home': 'Inicio',
    'tab.products': 'Productos',
    'tab.stockout': 'Salida',
    'tab.cart': 'Carrito',
    'tab.mine': 'Mi Cuenta',

    'index.searchPlaceholder': 'Buscar por nombre/SKU...',
    'index.categories': 'Categorías',
    'index.hotProducts': 'Productos Populares',

    'products.searchPlaceholder': 'Buscar productos...',

    'detail.stockInfo': '（Stock: {count} uds.）',
    'detail.goCart': '🛒 Carrito',
    'detail.addCart': 'Agregar al Carrito',
    'detail.outOfStock': 'Sin Stock',

    'cart.title': 'Carrito',
    'cart.itemCount': '{count} artículos',
    'cart.clear': 'Vaciar',
    'cart.subtotal': 'Subtotal',
    'cart.tip1': '💡 Presente esta lista en la tienda',
    'cart.tip2': 'El vendedor creará su pedido en la tienda',
    'cart.contactStore': '📞 Contactar Tienda',
    'cart.emptyIcon': '🛒',
    'cart.emptyText': 'El carrito está vacío',
    'cart.goShop': 'Ver Productos',
    'cart.confirmRemove': '¿Eliminar este artículo?',
    'cart.confirmClear': '¿Vaciar el carrito?',
    'cart.maxStock': 'Stock máximo alcanzado',
    'cart.added': 'Agregado al carrito',

    'mine.storeName': 'Repuestos Auto',
    'mine.storeSub': 'Repuestos de calidad, compra en tienda',
    'mine.myInfo': 'Mi Información (opcional)',
    'mine.name': 'Nombre',
    'mine.namePlaceholder': 'Para contactarle',
    'mine.phone': 'Teléfono',
    'mine.phonePlaceholder': 'Para contactarle',
    'mine.saved': 'Guardado',
    'mine.callStore': '📞 Llamar a la Tienda',
    'mine.language': '语言 / Idioma',
    'mine.langZh': '中文',
    'mine.langEs': 'Español',

    'stockout.scanBtn': 'Escanear Salida',
    'stockout.scanTip': 'Escanee el código de barras o ingrese SKU',
    'stockout.manualInput': 'Entrada manual',
    'stockout.skuPlaceholder': 'Ingrese código SKU',
    'stockout.currentStock': 'Stock actual',
    'stockout.outQty': 'Cantidad de salida',
    'stockout.remark': 'Observación',
    'stockout.remarkPlaceholder': 'Motivo de salida (opcional)',
    'stockout.confirmOut': 'Confirmar Salida',
    'stockout.noProduct': 'Producto no encontrado',
    'stockout.outSuccess': 'Salida exitosa',
    'stockout.stockNotEnough': 'Stock insuficiente'
  }
}

/**
 * 获取当前语言
 */
function getLocale() {
  return wx.getStorageSync('locale') || 'zh'
}

/**
 * 设置语言
 */
function setLocale(locale) {
  wx.setStorageSync('locale', locale)
}

/**
 * 翻译函数
 * @param {string} key - 翻译键
 * @param {object} params - 替换参数，如 { count: 5 }
 * @returns {string}
 */
function t(key, params) {
  const locale = getLocale()
  let text = messages[locale]?.[key] || messages['zh']?.[key] || key
  if (params) {
    Object.keys(params).forEach(k => {
      text = text.replace(`{${k}}`, params[k])
    })
  }
  return text
}

module.exports = {
  getLocale,
  setLocale,
  t,
  messages
}
