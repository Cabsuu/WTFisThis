1. **Add `TpaManager.java` in `A1/src/main/java/org/jerae/a1/`**
   - Create a class to handle teleportation requests (tpa, tpahere, tpaall) and their states.
   - It will map target players to active teleport requests (requester, request type).

2. **Add Teleportation Commands in `Commands.java`**
   - Implement `tpa`, `tpahere`, `tpyes`, `tpno`, `tpaall`.
   - Update `plugin.yml` to register these commands and their aliases.
   - For `/tpa <player>`, check permission `a1.tpa`, set request, and send messages to requester and target. The target should receive a message containing the `[tpyes]` and `[tpno]` textholders.
   - For `/tpahere <player>`, check permission `a1.tpahere`, set request, and send messages.
   - For `/tpaall`, check permission `a1.tpaall`, send request to all online players.
   - For `/tpyes`, handle the pending request (teleport player to target or target to player based on request type).
   - For `/tpno`, deny the request and notify the requester.

3. **Update `plugin.yml` with the new commands and aliases**
   - Add `tpa`, `tpahere`, `tpyes`, `tpno`, `tpaall` with aliases and descriptions.

4. **Update `config.yml`**
   - Add `nickname-prefix: "*"` setting with a description.
   - Add `nick-character-limit: 16` setting with a description. Don't let color codes affect it (using `stripTags`).
   - Add a description for the `afk-cooldown` setting.

5. **Update `message.yml`**
   - Add new messages for the teleportation feature.
     - `tpa-request-sent`, `tpa-request-received`, `tpahere-request-sent`, `tpahere-request-received`, `tpaall-request-sent`, `tpa-accepted`, `tpa-denied`, `no-pending-request`, `player-not-found`, `tpa-teleporting`, `tpa-request-expired` (optional).
   - Ensure to use placeholders (e.g. `%player_username%`).

6. **Update `textholder.yml` with `tpyes` and `tpno`**
   - Add a `tpyes` textholder with `[Accept]` text, click event running `/tpyes`.
   - Add a `tpno` textholder with `[Deny]` text, click event running `/tpno`.

7. **Update Nickname Logic in `Commands.java` and `A1.java`**
   - When checking the character limit in `/nick`, apply `nick-character-limit` from `config.yml`. Use `stripTags` to count raw characters.
   - Bypass character limit if the player has `a1.nick.bypasslimit` permission.
   - When applying the nickname in `onPlayerJoin` and `/nick`, add the `nickname-prefix` if the player does not have `a1.nick.hideprefix` permission.

8. **Ensure Memory Instructions**
   - Use `A3API.parseToString` or `A3API.parse` for placeholders.
   - Permissions are checked manually in code, not in `plugin.yml`.
   - Update config files correctly, preserving user settings.

9. **Pre-commit and Test**
   - Write tests for the `TpaManager` or mock the behavior.
   - Run tests and verify the code compiles and works.

10. **Submit changes**
