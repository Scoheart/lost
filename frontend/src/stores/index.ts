// stores/index.ts
// This file serves as the entry point for all Pinia stores

import { useUserStore } from './user'
import { useLostItemsStore } from './lostItems'
import { useFoundItemsStore } from './foundItems'
import { useAnnouncementsStore } from './announcements'
import { useForumStore } from './forum'
import { useMessageStore } from './messages'

export {
  useUserStore,
  useLostItemsStore,
  useFoundItemsStore,
  useAnnouncementsStore,
  useForumStore,
  useMessageStore
}
