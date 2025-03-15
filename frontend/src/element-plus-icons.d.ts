declare module '@element-plus/icons-vue' {
  import { DefineComponent } from 'vue'

  // Define a generic icon component type
  const IconComponent: DefineComponent<{}, {}, any>

  // Export all icons that are used in the project
  export const User: typeof IconComponent
  export const Lock: typeof IconComponent
  export const Search: typeof IconComponent
  export const Bell: typeof IconComponent
  export const ChatDotRound: typeof IconComponent
  export const Location: typeof IconComponent
  export const Calendar: typeof IconComponent
  export const Picture: typeof IconComponent
  export const Check: typeof IconComponent
  export const Timer: typeof IconComponent
  export const Setting: typeof IconComponent
  export const Collection: typeof IconComponent
  export const Upload: typeof IconComponent
  export const Phone: typeof IconComponent
  export const Plus: typeof IconComponent
  export const Edit: typeof IconComponent
  export const Message: typeof IconComponent
  export const Delete: typeof IconComponent
  export const UserFilled: typeof IconComponent
  export const Top: typeof IconComponent
  export const Document: typeof IconComponent
  export const SwitchButton: typeof IconComponent
  export const ArrowDown: typeof IconComponent
  export const HomeFilled: typeof IconComponent
  export const Map: typeof IconComponent

  // Allow importing any other icon
  export default IconComponent
}
