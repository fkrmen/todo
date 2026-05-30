import { createApp } from 'vue'
import { ElAvatar } from 'element-plus/es/components/avatar/index'
import { ElButton } from 'element-plus/es/components/button/index'
import { ElDatePicker } from 'element-plus/es/components/date-picker/index'
import { ElDialog } from 'element-plus/es/components/dialog/index'
import { ElDrawer } from 'element-plus/es/components/drawer/index'
import { ElDropdown, ElDropdownItem, ElDropdownMenu } from 'element-plus/es/components/dropdown/index'
import { ElForm, ElFormItem } from 'element-plus/es/components/form/index'
import { ElIcon } from 'element-plus/es/components/icon/index'
import { ElInput } from 'element-plus/es/components/input/index'
import { ElOption } from 'element-plus/es/components/select/index'
import { ElProgress } from 'element-plus/es/components/progress/index'
import { ElSelect } from 'element-plus/es/components/select/index'
import { ElTable, ElTableColumn } from 'element-plus/es/components/table/index'
import { ElTag } from 'element-plus/es/components/tag/index'
import 'element-plus/es/components/avatar/style/css'
import 'element-plus/es/components/button/style/css'
import 'element-plus/es/components/date-picker/style/css'
import 'element-plus/es/components/dialog/style/css'
import 'element-plus/es/components/drawer/style/css'
import 'element-plus/es/components/dropdown/style/css'
import 'element-plus/es/components/form/style/css'
import 'element-plus/es/components/icon/style/css'
import 'element-plus/es/components/input/style/css'
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import 'element-plus/es/components/option/style/css'
import 'element-plus/es/components/progress/style/css'
import 'element-plus/es/components/select/style/css'
import 'element-plus/es/components/table/style/css'
import 'element-plus/es/components/tag/style/css'
import './styles/theme.css'
import {
  ArrowDown,
  Bell,
  ChatLineSquare,
  Check,
  CircleCheck,
  Clock,
  Delete,
  Document,
  Edit,
  FolderOpened,
  List,
  Lock,
  Message,
  Picture,
  Plus,
  Setting,
  SwitchButton,
  TrendCharts,
  User
} from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

const globalComponents = [
  ElAvatar,
  ElButton,
  ElDatePicker,
  ElDialog,
  ElDrawer,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElOption,
  ElProgress,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag
]

const globalIcons = {
  ArrowDown,
  Bell,
  ChatLineSquare,
  Check,
  CircleCheck,
  Clock,
  Delete,
  Document,
  Edit,
  FolderOpened,
  List,
  Lock,
  Message,
  Picture,
  Plus,
  Setting,
  SwitchButton,
  TrendCharts,
  User
}

for (const component of globalComponents) {
  app.component(component.name, component)
}

for (const [name, component] of Object.entries(globalIcons)) {
  app.component(name, component)
}

app.use(router)
app.mount('#app')
