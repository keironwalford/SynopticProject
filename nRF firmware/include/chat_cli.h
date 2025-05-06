#ifndef CHAT_CLI_H_
#define CHAT_CLI_H_

#include <zephyr/bluetooth/mesh.h>
#include "chat_cli_op.h"

#define COMPANY_ID 0xFFFF 
#define MODEL_ID   0x0001

struct bt_mesh_chat_cli;

enum bt_mesh_chat_cli_presence {
    BT_MESH_CHAT_CLI_PRESENCE_AVAILABLE,
    BT_MESH_CHAT_CLI_PRESENCE_AWAY,
    BT_MESH_CHAT_CLI_PRESENCE_DO_NOT_DISTURB,
    BT_MESH_CHAT_CLI_PRESENCE_INACTIVE,
};

struct bt_mesh_chat_cli_handlers {
    void (*start)(struct bt_mesh_chat_cli *cli);
    void (*presence)(struct bt_mesh_chat_cli *cli, struct bt_mesh_msg_ctx *ctx,
                     enum bt_mesh_chat_cli_presence presence);
    void (*message)(struct bt_mesh_chat_cli *cli, struct bt_mesh_msg_ctx *ctx,
                    const uint8_t *msg);
    void (*private_message)(struct bt_mesh_chat_cli *cli, struct bt_mesh_msg_ctx *ctx,
                            const uint8_t *msg);
    void (*message_reply)(struct bt_mesh_chat_cli *cli, struct bt_mesh_msg_ctx *ctx);
};

struct bt_mesh_chat_cli {
    struct bt_mesh_model *model;
    struct bt_mesh_model_pub pub;
    struct net_buf_simple *msg;
    struct net_buf_simple pub_msg;
    const struct bt_mesh_chat_cli_handlers *handlers;
    enum bt_mesh_chat_cli_presence presence;
    uint8_t buf[BT_MESH_TX_SDU_MAX];
};

#define BT_MESH_MODEL_CHAT_CLI(cli) \
    BT_MESH_MODEL_VND_CB(COMPANY_ID, MODEL_ID, _bt_mesh_chat_cli_op, &(cli)->pub, cli, &_bt_mesh_chat_cli_cb)

/* Function declarations */
int bt_mesh_chat_cli_message_send(struct bt_mesh_chat_cli *cli, const uint8_t *msg);
int bt_mesh_chat_cli_private_message_send(struct bt_mesh_chat_cli *cli, uint16_t addr, const uint8_t *msg);
int bt_mesh_chat_cli_presence_set(struct bt_mesh_chat_cli *cli, enum bt_mesh_chat_cli_presence presence);
int bt_mesh_chat_cli_presence_get(struct bt_mesh_chat_cli *cli, uint16_t addr);
int bt_mesh_chat_cli_send(struct bt_mesh_chat_cli *cli, const char *msg, const struct bt_mesh_msg_ctx *ctx);

/* Externs required by the macro */
extern const struct bt_mesh_model_op _bt_mesh_chat_cli_op[];
extern const struct bt_mesh_model_cb _bt_mesh_chat_cli_cb;

#endif /* CHAT_CLI_H_ */
