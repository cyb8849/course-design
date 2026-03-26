import { defineStore } from 'pinia'
import axios from 'axios'

// 用户状态管理
export const useUserStore = defineStore('user', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    token: localStorage.getItem('token') || '',
    loading: false
  }),
  getters: {
    isLoggedIn: (state) => !!state.token && !!state.user,
    currentUser: (state) => state.user
  },
  actions: {
    async login(username, password, role) {
      this.loading = true
      try {
        const response = await axios.post('/auth/login', { username, password, role })
        const { token, user } = response.data.data || {}
        this.token = token
        this.user = user
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(user))
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async register(userData) {
      this.loading = true
      try {
        const response = await axios.post('/auth/register', userData)
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
    async getCurrentUser() {
      if (!this.token) return null
      try {
        const response = await axios.get('/auth/currentUser')
        this.user = response.data.data
        return this.user
      } catch (error) {
        this.logout()
        throw error
      }
    }
  }
})

// 商品状态管理
export const useProductStore = defineStore('product', {
  state: () => ({
    products: [],
    categories: [],
    currentProduct: null,
    loading: false
  }),
  actions: {
    async getProducts(page = 1, size = 10, categoryId = null, keyword = '', sortBy = 'default') {
      this.loading = true
      try {
        const response = await axios.get('/customer/products', {
          params: { page, size, categoryId, keyword, sortBy }
        })
        this.products = response.data?.data?.records || []
        return response.data || { data: { records: [] } }
      } catch (error) {
        this.products = []
        throw error
      } finally {
        this.loading = false
      }
    },
    async getProductDetail(id) {
      this.loading = true
      try {
        const response = await axios.get(`/customer/products/${id}`)
        this.currentProduct = response.data.data
        return this.currentProduct
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async getCategories() {
      try {
        const response = await axios.get('/customer/categories')
        this.categories = response.data?.data || []
        return this.categories
      } catch (error) {
        this.categories = []
        throw error
      }
    }
  }
})

// 购物车状态管理
export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false
  }),
  getters: {
    totalItems: (state) => state.items.length,
    totalAmount: (state) => {
      return state.items.reduce((total, item) => total + item.price * item.quantity, 0)
    }
  },
  actions: {
    async getCartItems(userId) {
      this.loading = true
      try {
        const response = await axios.get(`/customer/cart/${userId}`)
        this.items = response.data?.data || []
        return this.items
      } catch (error) {
        this.items = []
        throw error
      } finally {
        this.loading = false
      }
    },
    async addToCart(productId, quantity, userId) {
      this.loading = true
      try {
        const response = await axios.post('/customer/cart', {
          productId,
          quantity,
          userId
        })
        await this.getCartItems(userId)
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async updateCartItem(id, quantity) {
      this.loading = true
      try {
        const response = await axios.put(`/customer/cart/${id}`, null, {
          params: { quantity }
        })
        const index = this.items.findIndex(item => item.id === id)
        if (index !== -1) {
          this.items[index].quantity = quantity
        }
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async removeFromCart(id) {
      this.loading = true
      try {
        const response = await axios.delete(`/customer/cart/${id}`)
        this.items = this.items.filter(item => item.id !== id)
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async clearCart(userId) {
      this.loading = true
      try {
        const response = await axios.delete(`/customer/cart/clear/${userId}`)
        this.items = []
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})

// 订单状态管理
export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [],
    currentOrder: null,
    loading: false
  }),
  actions: {
    async getOrders(userId, page = 1, size = 10, status = null) {
      this.loading = true
      try {
        const response = await axios.get('/customer/orders', {
          params: { userId, page, size, status }
        })
        this.orders = response.data.data.records
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async getOrderDetail(orderId) {
      this.loading = true
      try {
        const response = await axios.get(`/customer/orders/${orderId}`)
        this.currentOrder = response.data.data
        return this.currentOrder
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async createOrder(orderData) {
      this.loading = true
      try {
        const response = await axios.post('/customer/orders', orderData)
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async payOrder(orderId) {
      this.loading = true
      try {
        const response = await axios.put(`/customer/orders/${orderId}/pay`)
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async cancelOrder(orderId) {
      this.loading = true
      try {
        const response = await axios.put(`/customer/orders/${orderId}/cancel`)
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async confirmReceipt(orderId) {
      this.loading = true
      try {
        const response = await axios.put(`/customer/orders/${orderId}/confirm`)
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async deleteOrder(orderId) {
      this.loading = true
      try {
        const response = await axios.delete(`/customer/orders/${orderId}`)
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})

// 地址状态管理
export const useAddressStore = defineStore('address', {
  state: () => ({
    addresses: [],
    loading: false
  }),
  actions: {
    async getAddresses(userId) {
      this.loading = true
      try {
        const response = await axios.get(`/customer/addresses/${userId}`)
        this.addresses = response.data?.data || []
        return this.addresses
      } catch (error) {
        this.addresses = []
        throw error
      } finally {
        this.loading = false
      }
    },
    async addAddress(addressData) {
      this.loading = true
      try {
        const response = await axios.post('/customer/addresses', addressData)
        await this.getAddresses(addressData.userId)
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async updateAddress(id, addressData) {
      this.loading = true
      try {
        const response = await axios.put(`/customer/addresses/${id}`, addressData)
        await this.getAddresses(addressData.userId)
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async deleteAddress(id) {
      this.loading = true
      try {
        const response = await axios.delete(`/customer/addresses/${id}`)
        this.addresses = this.addresses.filter(addr => addr.id !== id)
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})

// 统计数据状态管理
export const useStatisticsStore = defineStore('statistics', {
  state: () => ({
    salesData: null,
    categoryData: null,
    farmerData: null,
    inventoryData: null,
    overviewData: null,
    orderStatusData: null,
    loading: false
  }),
  actions: {
    async getSalesStatistics(days = 30) {
      this.loading = true
      try {
        const response = await axios.get('/admin/statistics/sales', {
          params: { days }
        })
        this.salesData = response.data.data
        return this.salesData
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async getCategoryStatistics() {
      this.loading = true
      try {
        const response = await axios.get('/admin/statistics/category')
        this.categoryData = response.data.data
        return this.categoryData
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async getFarmerStatistics() {
      this.loading = true
      try {
        const response = await axios.get('/admin/statistics/farmers')
        this.farmerData = response.data.data
        return this.farmerData
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async getInventoryStatistics() {
      this.loading = true
      try {
        const response = await axios.get('/admin/statistics/inventory')
        this.inventoryData = response.data.data
        return this.inventoryData
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async getOverviewStatistics() {
      this.loading = true
      try {
        const response = await axios.get('/admin/statistics/overview')
        this.overviewData = response.data.data
        return this.overviewData
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    async getOrderStatusStatistics() {
      this.loading = true
      try {
        const response = await axios.get('/admin/statistics/order-status')
        this.orderStatusData = response.data.data
        return this.orderStatusData
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})