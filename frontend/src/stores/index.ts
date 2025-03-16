// stores/index.ts
// This file serves as the entry point for all Pinia stores

import { useUserStore } from './user'
import { useLostItemsStore } from './lostItems'
import { useFoundItemsStore } from './foundItems'
import { useAnnouncementsStore } from './announcements'
import { useMessageStore } from './messages'
import { useClaimsStore } from './claims'

export {
  useUserStore,
  useLostItemsStore,
  useFoundItemsStore,
  useAnnouncementsStore,
  useMessageStore,
  useClaimsStore
}
