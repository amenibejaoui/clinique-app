export const GlobalComponent = {
    // Api Calling
    API_URL: 'http://localhost:9090/api/',
    headerToken: { 'Authorization': `Bearer ${localStorage.getItem('token')}` },

    // Auth Api
    AUTH_API: 'http://localhost:9090/api/auth/',

    // Products Api
    product: 'apps/product',
    productDelete: 'apps/product/',

    // Orders Api
    order: 'apps/order',
    orderId: 'apps/order/',

    // Customers Api
    customer: 'apps/customer',

}