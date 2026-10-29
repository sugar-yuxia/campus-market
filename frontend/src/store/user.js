import { reactive } from 'vue'

const TOKEN_KEY = 'cm_token'
const USER_KEY = 'cm_user'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch (e) {
    return null
  }
}

const state = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  user: readUser()
})

function setSession(token, user) {
  state.token = token
  state.user = user
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

function setUser(user) {
  state.user = user
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

function logout() {
  state.token = ''
  state.user = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export default { state, setSession, setUser, logout }
