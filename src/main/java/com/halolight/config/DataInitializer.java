package com.halolight.config;

import com.halolight.domain.entity.*;
import com.halolight.domain.entity.enums.AttendeeStatus;
import com.halolight.domain.entity.enums.SharePermission;
import com.halolight.domain.entity.enums.UserStatus;
import com.halolight.domain.entity.id.ConversationParticipantId;
import com.halolight.domain.entity.id.EventAttendeeId;
import com.halolight.domain.entity.id.TeamMemberId;
import com.halolight.domain.entity.id.UserRoleId;
import com.halolight.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 数据初始化器 - 仅在 dev 环境下运行
 * 创建演示数据：用户、角色、权限、团队、文档、文件、日历事件、通知、消息等
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final DocumentRepository documentRepository;
    private final TagRepository tagRepository;
    private final StorageFileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final CalendarEventRepository eventRepository;
    private final EventAttendeeRepository attendeeRepository;
    private final NotificationRepository notificationRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;

    // 配置参数
    private static final int USER_COUNT = 30;
    private static final int DOCUMENT_COUNT = 30;
    private static final int FILE_COUNT = 40;
    private static final int EVENT_COUNT = 25;
    private static final int NOTIFICATION_COUNT = 60;
    private static final String DEFAULT_PASSWORD = "123456";

    // 中文名数据
    private static final String[] SURNAMES = {"张", "李", "王", "赵", "钱", "孙", "周", "吴", "郑", "冯", "陈", "韩", "杨", "沈", "魏", "蒋"};
    private static final String[] NAMES = {"伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "洋", "艳", "勇", "军", "杰", "涛", "明", "超", "秀英", "华", "慧", "建国"};
    private static final String[] DEPARTMENTS = {"技术部", "产品部", "设计部", "市场部", "运营部", "财务部", "人事部", "客服部", "研发部", "测试部"};
    private static final String[] POSITIONS = {"工程师", "高级工程师", "资深工程师", "技术专家", "产品经理", "设计师", "UI设计师", "交互设计师", "市场专员", "运营专员"};
    private static final String[] DOC_FOLDERS = {"项目文档", "设计资源", "技术文档", "报表", "会议记录"};
    private static final String[] DOC_TYPES = {"pdf", "doc", "image", "spreadsheet", "code", "other"};
    private static final String[] FILE_TYPES = {"image", "video", "audio", "archive", "document"};
    private static final String[] EVENT_TYPES = {"meeting", "task", "reminder", "holiday"};
    private static final String[] EVENT_COLORS = {"#6366f1", "#8b5cf6", "#ec4899", "#10b981", "#f59e0b", "#ef4444", "#3b82f6"};
    private static final String[] NOTIFICATION_TYPES = {"system", "message", "task", "alert", "user"};

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already has data, skipping initialization");
            return;
        }

        log.info("Starting data initialization...");

        try {
            // 1. 创建权限
            List<Permission> permissions = createPermissions();
            log.info("Created {} permissions", permissions.size());

            // 2. 创建角色
            List<Role> roles = createRoles(permissions);
            log.info("Created {} roles", roles.size());

            // 3. 创建用户
            List<User> users = createUsers(roles);
            log.info("Created {} users", users.size());

            // 4. 创建团队
            List<Team> teams = createTeams(users);
            log.info("Created {} teams", teams.size());

            // 5. 创建标签
            List<Tag> tags = createTags();
            log.info("Created {} tags", tags.size());

            // 6. 创建文档
            List<Document> documents = createDocuments(users, teams, tags);
            log.info("Created {} documents", documents.size());

            // 7. 创建文件夹和文件
            List<StorageFile> files = createFoldersAndFiles(users, teams);
            log.info("Created {} files", files.size());

            // 8. 创建日历事件
            List<CalendarEvent> events = createCalendarEvents(users, teams);
            log.info("Created {} calendar events", events.size());

            // 9. 创建通知
            List<Notification> notifications = createNotifications(users);
            log.info("Created {} notifications", notifications.size());

            // 10. 创建会话和消息
            createConversationsAndMessages(users, teams);
            log.info("Created conversations and messages");

            // 11. 创建活动日志
            List<ActivityLog> logs = createActivityLogs(users);
            log.info("Created {} activity logs", logs.size());

            log.info("Data initialization completed successfully!");

        } catch (Exception e) {
            log.error("Data initialization failed", e);
            throw e;
        }
    }

    private List<Permission> createPermissions() {
        String[] resources = {"users", "roles", "teams", "documents", "files", "calendar", "notifications", "messages", "dashboard"};
        String[] actions = {"create", "read", "update", "delete", "manage"};

        List<Permission> permissions = new ArrayList<>();
        for (String resource : resources) {
            for (String action : actions) {
                Permission permission = Permission.builder()
                        .action(action)
                        .resource(resource)
                        .description(String.format("可以%s %s", translateAction(action), translateResource(resource)))
                        .build();
                permissions.add(permissionRepository.save(permission));
            }
        }

        // 添加通配符权限
        Permission adminPermission = Permission.builder()
                .action("*")
                .resource("*")
                .description("超级管理员权限")
                .build();
        permissions.add(permissionRepository.save(adminPermission));

        return permissions;
    }

    private List<Role> createRoles(List<Permission> permissions) {
        List<Role> roles = new ArrayList<>();

        // Admin 角色 - 拥有所有权限
        Role adminRole = Role.builder()
                .name("admin")
                .label("管理员")
                .description("系统管理员，拥有所有权限")
                .build();
        adminRole = roleRepository.save(adminRole);

        // 为管理员分配所有权限
        for (Permission p : permissions) {
            RolePermission rp = RolePermission.builder()
                    .role(adminRole)
                    .permission(p)
                    .build();
            adminRole.getPermissions().add(rp);
        }
        roles.add(roleRepository.save(adminRole));

        // User 角色 - 基本权限
        Role userRole = Role.builder()
                .name("user")
                .label("普通用户")
                .description("普通用户，拥有基本操作权限")
                .build();
        userRole = roleRepository.save(userRole);

        // 为普通用户分配读取权限
        final Role savedUserRole = userRole;
        permissions.stream()
                .filter(p -> "read".equals(p.getAction()) || "create".equals(p.getAction()))
                .forEach(p -> {
                    RolePermission rp = RolePermission.builder()
                            .role(savedUserRole)
                            .permission(p)
                            .build();
                    savedUserRole.getPermissions().add(rp);
                });
        roles.add(roleRepository.save(userRole));

        // Editor 角色
        Role editorRole = Role.builder()
                .name("editor")
                .label("编辑者")
                .description("可以编辑内容")
                .build();
        editorRole = roleRepository.save(editorRole);

        final Role savedEditorRole = editorRole;
        permissions.stream()
                .filter(p -> !p.getAction().equals("*") && !p.getAction().equals("manage"))
                .forEach(p -> {
                    RolePermission rp = RolePermission.builder()
                            .role(savedEditorRole)
                            .permission(p)
                            .build();
                    savedEditorRole.getPermissions().add(rp);
                });
        roles.add(roleRepository.save(editorRole));

        return roles;
    }

    private List<User> createUsers(List<Role> roles) {
        List<User> users = new ArrayList<>();
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        Role adminRole = roles.stream().filter(r -> "admin".equals(r.getName())).findFirst().orElse(roles.get(0));
        Role userRole = roles.stream().filter(r -> "user".equals(r.getName())).findFirst().orElse(roles.get(1));

        // 创建管理员用户
        User admin = User.builder()
                .email("admin@halolight.h7ml.cn")
                .username("admin")
                .password(encodedPassword)
                .name("系统管理员")
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=admin")
                .status(UserStatus.ACTIVE)
                .department("技术部")
                .position("技术总监")
                .bio("系统管理员账号")
                .quotaUsed(BigInteger.ZERO)
                .build();
        admin = userRepository.save(admin);

        // 分配管理员角色
        UserRole adminUserRole = UserRole.builder()
                .id(new UserRoleId(admin.getId(), adminRole.getId()))
                .user(admin)
                .role(adminRole)
                .build();
        userRoleRepository.save(adminUserRole);
        users.add(admin);

        // 创建演示用户
        User demo = User.builder()
                .email("demo@halolight.h7ml.cn")
                .username("demo")
                .password(encodedPassword)
                .name("演示用户")
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=demo")
                .status(UserStatus.ACTIVE)
                .department("产品部")
                .position("产品经理")
                .bio("演示账号，用于体验系统功能")
                .quotaUsed(BigInteger.ZERO)
                .build();
        demo = userRepository.save(demo);

        UserRole demoUserRole = UserRole.builder()
                .id(new UserRoleId(demo.getId(), userRole.getId()))
                .user(demo)
                .role(userRole)
                .build();
        userRoleRepository.save(demoUserRole);
        users.add(demo);

        // 创建普通用户
        UserStatus[] statuses = {UserStatus.ACTIVE, UserStatus.ACTIVE, UserStatus.ACTIVE, UserStatus.INACTIVE, UserStatus.SUSPENDED};
        for (int i = 1; i <= USER_COUNT - 2; i++) {
            User user = User.builder()
                    .email(String.format("user%d@halolight.h7ml.cn", i))
                    .username(String.format("user%d", i))
                    .password(encodedPassword)
                    .name(generateChineseName())
                    .avatar(String.format("https://api.dicebear.com/7.x/avataaars/svg?seed=user%d", i))
                    .status(randomPick(statuses))
                    .department(randomPick(DEPARTMENTS))
                    .position(randomPick(POSITIONS))
                    .bio("这是一个普通用户账号")
                    .quotaUsed(BigInteger.valueOf(random.nextInt(1000000)))
                    .build();
            user = userRepository.save(user);

            UserRole ur = UserRole.builder()
                    .id(new UserRoleId(user.getId(), userRole.getId()))
                    .user(user)
                    .role(userRole)
                    .build();
            userRoleRepository.save(ur);
            users.add(user);
        }

        return users;
    }

    private List<Team> createTeams(List<User> users) {
        String[] teamNames = {"研发一组", "产品设计组", "市场推广组", "客户支持组", "数据分析组"};
        List<Team> teams = new ArrayList<>();

        for (int i = 0; i < teamNames.length; i++) {
            User owner = users.get(i % users.size());
            Team team = Team.builder()
                    .name(teamNames[i])
                    .description(teamNames[i] + " - 负责相关业务")
                    .avatar(String.format("https://api.dicebear.com/7.x/identicon/svg?seed=team%d", i))
                    .owner(owner)
                    .build();
            team = teamRepository.save(team);

            // 添加团队成员
            Set<User> members = new HashSet<>();
            members.add(owner);
            int memberCount = 3 + random.nextInt(5);
            while (members.size() < memberCount && members.size() < users.size()) {
                members.add(users.get(random.nextInt(users.size())));
            }

            for (User member : members) {
                String roleId = member.equals(owner) ? "owner" : (random.nextBoolean() ? "admin" : "member");
                TeamMember tm = TeamMember.builder()
                        .id(new TeamMemberId(team.getId(), member.getId()))
                        .team(team)
                        .user(member)
                        .roleId(roleId)
                        .build();
                teamMemberRepository.save(tm);
            }

            teams.add(team);
        }

        return teams;
    }

    private List<Tag> createTags() {
        String[] tagNames = {"重要", "紧急", "待审核", "已完成", "进行中", "技术", "产品", "设计", "市场", "运营"};

        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            Tag tag = Tag.builder()
                    .name(tagName)
                    .build();
            tags.add(tagRepository.save(tag));
        }
        return tags;
    }

    private List<Document> createDocuments(List<User> users, List<Team> teams, List<Tag> tags) {
        String[] titles = {
            "API 使用指南", "产品规划文档", "UI设计规范", "周会纪要", "数据库设计",
            "前端架构说明", "用户调研报告", "产品原型设计", "季度销售报表", "技术分享记录"
        };

        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < DOCUMENT_COUNT; i++) {
            User owner = users.get(random.nextInt(users.size()));
            Team team = random.nextBoolean() ? teams.get(random.nextInt(teams.size())) : null;

            Document doc = Document.builder()
                    .title(titles[i % titles.length] + (i >= titles.length ? " v" + (i / titles.length + 1) : ""))
                    .content("# " + titles[i % titles.length] + "\n\n这是文档内容...")
                    .type(randomPick(DOC_TYPES))
                    .folder(randomPick(DOC_FOLDERS))
                    .size(BigInteger.valueOf(1024 + random.nextInt(10000000)))
                    .views(random.nextInt(1000))
                    .owner(owner)
                    .team(team)
                    .build();
            documents.add(documentRepository.save(doc));
        }
        return documents;
    }

    private List<StorageFile> createFoldersAndFiles(List<User> users, List<Team> teams) {
        // 创建根文件夹
        List<Folder> folders = new ArrayList<>();
        String[] folderNames = {"文档", "图片", "视频", "音乐", "下载", "项目资料"};

        for (User user : users.subList(0, Math.min(5, users.size()))) {
            for (String name : folderNames) {
                Folder folder = Folder.builder()
                        .name(name)
                        .owner(user)
                        .team(random.nextBoolean() && !teams.isEmpty() ? teams.get(random.nextInt(teams.size())) : null)
                        .build();
                folders.add(folderRepository.save(folder));
            }
        }

        // 创建文件
        List<StorageFile> files = new ArrayList<>();
        String[] fileNames = {"report.pdf", "photo.jpg", "video.mp4", "music.mp3", "archive.zip", "document.docx"};
        String[] mimeTypes = {"application/pdf", "image/jpeg", "video/mp4", "audio/mpeg", "application/zip", "application/msword"};

        for (int i = 0; i < FILE_COUNT; i++) {
            User owner = users.get(random.nextInt(users.size()));
            Folder folder = !folders.isEmpty() && random.nextBoolean() ? folders.get(random.nextInt(folders.size())) : null;

            StorageFile file = StorageFile.builder()
                    .name(fileNames[i % fileNames.length].replace(".", i + "."))
                    .size(BigInteger.valueOf(1024 + random.nextInt(50000000)))
                    .path("/uploads/" + UUID.randomUUID())
                    .type(randomPick(FILE_TYPES))
                    .owner(owner)
                    .folder(folder)
                    .team(random.nextBoolean() && !teams.isEmpty() ? teams.get(random.nextInt(teams.size())) : null)
                    .build();
            files.add(fileRepository.save(file));
        }
        return files;
    }

    private List<CalendarEvent> createCalendarEvents(List<User> users, List<Team> teams) {
        String[] eventTitles = {"团队周会", "产品评审", "技术分享", "项目启动会", "季度总结", "一对一沟通", "培训课程"};
        List<CalendarEvent> events = new ArrayList<>();

        for (int i = 0; i < EVENT_COUNT; i++) {
            User organizer = users.get(random.nextInt(users.size()));
            Instant startTime = Instant.now().plus(random.nextInt(60) - 30, ChronoUnit.DAYS)
                    .plus(9 + random.nextInt(8), ChronoUnit.HOURS);
            Instant endTime = startTime.plus(1 + random.nextInt(2), ChronoUnit.HOURS);

            CalendarEvent event = CalendarEvent.builder()
                    .title(eventTitles[i % eventTitles.length])
                    .description("这是一个" + eventTitles[i % eventTitles.length] + "的详细描述")
                    .location("会议室 " + (1 + random.nextInt(10)))
                    .startTime(startTime)
                    .endTime(endTime)
                    .allDay(random.nextInt(10) < 2)
                    .color(randomPick(EVENT_COLORS))
                    .organizer(organizer)
                    .team(!teams.isEmpty() && random.nextBoolean() ? teams.get(random.nextInt(teams.size())) : null)
                    .build();
            event = eventRepository.save(event);

            // 添加参与者
            int attendeeCount = 2 + random.nextInt(5);
            Set<User> attendees = new HashSet<>();
            attendees.add(organizer);
            while (attendees.size() < attendeeCount) {
                attendees.add(users.get(random.nextInt(users.size())));
            }

            AttendeeStatus[] statuses = AttendeeStatus.values();
            for (User attendee : attendees) {
                EventAttendee ea = EventAttendee.builder()
                        .id(new EventAttendeeId(event.getId(), attendee.getId()))
                        .event(event)
                        .user(attendee)
                        .status(attendee.equals(organizer) ? AttendeeStatus.ACCEPTED : randomPick(statuses))
                        .build();
                attendeeRepository.save(ea);
            }

            events.add(event);
        }
        return events;
    }

    private List<Notification> createNotifications(List<User> users) {
        String[] titles = {"系统通知", "新消息提醒", "任务提醒", "安全警告", "用户通知"};
        String[] messages = {
            "系统将于今晚进行维护升级",
            "您有一条新消息",
            "您的任务即将到期",
            "检测到异常登录行为",
            "欢迎使用 HaloLight 系统"
        };

        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < NOTIFICATION_COUNT; i++) {
            User user = users.get(random.nextInt(users.size()));
            boolean isRead = random.nextBoolean();

            Notification notification = Notification.builder()
                    .user(user)
                    .title(titles[i % titles.length])
                    .message(messages[i % messages.length])
                    .type(randomPick(NOTIFICATION_TYPES))
                    .isRead(isRead)
                    .readAt(isRead ? Instant.now().minus(random.nextInt(24), ChronoUnit.HOURS) : null)
                    .actionUrl("/dashboard")
                    .build();
            notifications.add(notificationRepository.save(notification));
        }
        return notifications;
    }

    private void createConversationsAndMessages(List<User> users, List<Team> teams) {
        // 创建群聊
        for (int i = 0; i < 5; i++) {
            Team team = !teams.isEmpty() ? teams.get(i % teams.size()) : null;
            Conversation conv = Conversation.builder()
                    .name("群聊 " + (i + 1))
                    .isGroup(true)
                    .teamId(team != null ? team.getId() : null)
                    .build();
            conv = conversationRepository.save(conv);

            // 添加参与者
            int participantCount = 3 + random.nextInt(5);
            List<User> participants = new ArrayList<>();
            while (participants.size() < participantCount) {
                User u = users.get(random.nextInt(users.size()));
                if (!participants.contains(u)) {
                    participants.add(u);
                }
            }

            for (User p : participants) {
                ConversationParticipant cp = ConversationParticipant.builder()
                        .id(new ConversationParticipantId(conv.getId(), p.getId()))
                        .conversation(conv)
                        .user(p)
                        .build();
                participantRepository.save(cp);
            }

            // 添加消息
            int messageCount = 8 + random.nextInt(12);
            for (int j = 0; j < messageCount; j++) {
                User sender = participants.get(random.nextInt(participants.size()));
                Message msg = Message.builder()
                        .conversationId(conv.getId())
                        .senderId(sender.getId())
                        .sender(sender)
                        .content("这是第 " + (j + 1) + " 条消息")
                        .type("text")
                        .isEdited(false)
                        .build();
                messageRepository.save(msg);
            }
        }

        // 创建私聊
        for (int i = 0; i < 8 && i < users.size() - 1; i++) {
            Conversation conv = Conversation.builder()
                    .isGroup(false)
                    .build();
            conv = conversationRepository.save(conv);

            User user1 = users.get(i);
            User user2 = users.get(i + 1);

            participantRepository.save(ConversationParticipant.builder()
                    .id(new ConversationParticipantId(conv.getId(), user1.getId()))
                    .conversation(conv)
                    .user(user1)
                    .build());

            participantRepository.save(ConversationParticipant.builder()
                    .id(new ConversationParticipantId(conv.getId(), user2.getId()))
                    .conversation(conv)
                    .user(user2)
                    .build());

            // 添加消息
            int messageCount = 5 + random.nextInt(10);
            for (int j = 0; j < messageCount; j++) {
                User sender = j % 2 == 0 ? user1 : user2;
                Message msg = Message.builder()
                        .conversationId(conv.getId())
                        .senderId(sender.getId())
                        .sender(sender)
                        .content("私聊消息 " + (j + 1))
                        .type("text")
                        .isEdited(false)
                        .build();
                messageRepository.save(msg);
            }
        }
    }

    private List<ActivityLog> createActivityLogs(List<User> users) {
        String[] actions = {
            "user.login", "user.logout", "document.create", "document.update",
            "file.upload", "file.download", "team.create", "event.create"
        };

        List<ActivityLog> logs = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            User user = users.get(random.nextInt(users.size()));
            String action = randomPick(actions);

            ActivityLog log = ActivityLog.builder()
                    .user(user)
                    .action(action)
                    .resourceType(action.split("\\.")[0])
                    .resourceId(UUID.randomUUID().toString().substring(0, 25))
                    .details("用户执行了 " + action + " 操作")
                    .ipAddress("192.168.1." + random.nextInt(255))
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build();
            logs.add(activityLogRepository.save(log));
        }
        return logs;
    }

    // 辅助方法
    private String generateChineseName() {
        String surname = SURNAMES[random.nextInt(SURNAMES.length)];
        String name1 = NAMES[random.nextInt(NAMES.length)];
        String name2 = random.nextBoolean() ? NAMES[random.nextInt(NAMES.length)] : "";
        return surname + name1 + name2;
    }

    @SafeVarargs
    private <T> T randomPick(T... array) {
        return array[random.nextInt(array.length)];
    }

    private String translateAction(String action) {
        return switch (action) {
            case "create" -> "创建";
            case "read" -> "查看";
            case "update" -> "更新";
            case "delete" -> "删除";
            case "manage" -> "管理";
            default -> action;
        };
    }

    private String translateResource(String resource) {
        return switch (resource) {
            case "users" -> "用户";
            case "roles" -> "角色";
            case "teams" -> "团队";
            case "documents" -> "文档";
            case "files" -> "文件";
            case "calendar" -> "日历";
            case "notifications" -> "通知";
            case "messages" -> "消息";
            case "dashboard" -> "仪表盘";
            default -> resource;
        };
    }
}
