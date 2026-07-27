# 💬 Chat temps réel — Spring Boot WebSocket/STOMP + Angular

> Application de chat en temps réel avec plusieurs salons de discussion, construite pour maîtriser en profondeur le protocole WebSocket, STOMP/SockJS, l'architecture Pub/Sub, et leur intégration dans une infrastructure Docker avec reverse proxy — avec une architecture backend et frontend en couches découplées, testée à chaque niveau.

---

## 🎯 Objectif du projet

Ce projet explore un paradigme différent : la communication **temps réel bidirectionnelle**, où le serveur peut pousser des données au client sans que celui-ci les demande explicitement.

L'objectif n'était pas seulement "faire un chat qui marche", mais de comprendre et savoir expliquer :
- Pourquoi HTTP classique ne suffit pas pour du temps réel, et ce que WebSocket change fondamentalement
- Comment se déroule concrètement le handshake `HTTP → WebSocket` (`101 Switching Protocols`)
- Pourquoi STOMP et SockJS existent par-dessus WebSocket brut
- Comment fonctionne un système Pub/Sub (topics, souscriptions, diffusion)
- Comment faire passer un reverse proxy à travers un protocole avec upgrade de connexion — un point technique que HTTP classique ne pose jamais
- Comment structurer un backend et un frontend en couches réellement découplées et testables

---

## 🗺️ Architecture

```
                    Navigateur (2 onglets)
                  http://chat-app.local
                            │
                  (DNS → fichier hosts)
                            │
                            ▼
                  Reverse Proxy (NGINX)
             /              │              \
            /               │               \
   Frontend (Angular)   /api/ (REST)    /ws/ (WebSocket, upgrade)
                            │               │
                     public-net       private-net
                            │               │
                            └───────┬───────┘
                                    ▼
                         Backend (Spring Boot)
                          STOMP + SockJS
                                    │
                                    ▼
                              PostgreSQL
```

### Réseaux Docker isolés 

| Réseau | Contient | CIDR | Accessible depuis l'extérieur ? |
|---|---|---|---|
| `public-net` | Reverse Proxy, Frontend | `172.22.0.0/24` | ✅ Oui (port 80) |
| `private-net` | Backend, PostgreSQL | `172.23.0.0/24` (`internal: true`) | ❌ Non |

---

## 🧰 Stack technique

| Outil | Rôle |
|---|---|
| **Spring Boot  (Java 17)** | Backend, avec Spring WebSocket + STOMP |
| **STOMP** | Protocole de messagerie par-dessus WebSocket (topics, souscriptions) |
| **SockJS** | Repli automatique si WebSocket natif est bloqué |
| **Angular + `@stomp/stompjs`** | Frontend, client STOMP typé |
| **PostgreSQL** | Persistance de l'historique des messages |
| **Nginx** | Reverse proxy, avec gestion explicite du handshake WebSocket |
| **Docker / Docker Compose** | Isolation réseau, orchestration |
| **Springdoc OpenAPI (Swagger)** | Documentation et test de l'API REST (historique) |
| **JUnit 5 + Mockito + AssertJ** | Tests unitaires (service) et d'intégration (WebSocket) |

---

## 🧠 Concepts clés — ce que ce projet démontre

### 1. HTTP classique vs WebSocket
HTTP : requête → réponse → connexion fermée, à chaque échange. WebSocket : une connexion ouverte une seule fois, puis des messages dans les deux sens (**full-duplex**), sans jamais la refermer. C'est ce qui permet au serveur de **pousser** un message dès qu'il existe, sans que le client fasse du polling.

### 2. Le handshake
Une connexion WebSocket démarre comme une requête HTTP normale, avec des headers spéciaux :
```
GET /ws HTTP/1.1
Upgrade: websocket
Connection: Upgrade
```
Le serveur répond `101 Switching Protocols` — après quoi la connexion TCP reste ouverte pour des frames WebSocket, plus des requêtes/réponses HTTP classiques.

### 3. STOMP et SockJS
- **STOMP** ajoute une couche de convention par-dessus WebSocket brut : des commandes (`CONNECT`, `SUBSCRIBE`, `SEND`) et des **destinations** nommées (`/topic/room.general`)
- **SockJS** détecte si WebSocket est disponible et bascule discrètement vers un repli sinon — transparent pour le code applicatif

### 4. Pub/Sub
Les clients ne se parlent jamais directement — ils s'abonnent à des **topics**, et le serveur relaie chaque message à tous les abonnés du même topic. `/app/...` = client vers serveur, `/topic/...` = diffusion serveur vers clients abonnés.

### 5. Reconnexion côté client
Une connexion WebSocket peut se couper à tout moment. Le client doit : reconnecter automatiquement (backoff), re-souscrire aux topics actifs après reconnexion, et afficher un statut de connexion clair pour l'utilisateur.

---

## 📁 Architecture logicielle — backend en couches découplées

```
com.aymane.chatapp
├── config/WebSocketConfig.java        → endpoint STOMP + broker, configuration pure
├── controller/
│   ├── ChatController.java            → @MessageMapping (STOMP) : envoi + JOIN
│   └── RoomHistoryController.java     → @RestController (REST) : historique, documenté Swagger
├── dto/
│   ├── ChatMessageRequest.java        → validé (@NotBlank), ce que le client envoie
│   └── ChatMessageResponse.java       → ce que le serveur diffuse
├── model/
│   ├── ChatMessage.java               → entité JPA, immuable (pas de setters)
│   └── MessageType.java               → enum CHAT / JOIN / LEAVE
├── repository/ChatMessageRepository.java
├── mapper/ChatMessageMapper.java      → conversion Entité → DTO, isolée
├── service/
│   ├── ChatService.java               → interface (contrat)
│   └── impl/ChatServiceImpl.java      → implémentation, injectée par constructeur
└── listener/WebSocketEventListener.java → JOIN/LEAVE automatiques à la (dé)connexion
```

**Pourquoi cette séparation ?**
- **DTO ≠ Entité** — l'entité décrit le stockage, le DTO décrit le contrat réseau ; les coupler fait fuir des détails internes et casse au moindre changement de schéma
- **Interface + implémentation** pour le service — permet de changer l'implémentation (ex: passer à un broker externe RabbitMQ) sans toucher au contrôleur, et rend le service mockable pour les tests
- **Mapper isolé** — Single Responsibility : le service gère la logique métier, le mapper gère uniquement la transformation de forme
- **Listener séparé** — la déconnexion est un événement transverse au niveau de la session, pas une route applicative ; Spring expose un mécanisme d'événements dédié pour ça

## 📁 Architecture logicielle — frontend typé, sans `any`

```
src/app/
├── models/chat-message.model.ts          → interfaces + enum, miroir des DTOs backend
├── services/
│   ├── websocket-connection.service.ts   → couche transport (connexion, reconnexion, pub/sub bas niveau)
│   ├── chat.service.ts                   → couche métier (salons, messages)
│   └── room-history.service.ts           → appel REST classique pour l'historique
└── components/
    ├── room-selector/                    → sélection du salon (émet un événement)
    └── chat-room/                        → affichage + envoi, consomme les services
```

**Pourquoi deux services séparés (`WebSocketConnectionService` / `ChatService`) ?** Le premier ne connaît rien du "chat" — juste se connecter, s'abonner, publier. Le second connaît le vocabulaire métier mais ignore les détails STOMP/SockJS. Remplacer la librairie WebSocket ne toucherait qu'un seul fichier, jamais les composants.

**Zéro `any` dans le code** — le seul cast présent (`JSON.parse(message.body) as ChatMessage`) est justifié : c'est la frontière exacte entre données réseau non typées et données applicatives typées, un point de passage obligé et documenté plutôt qu'une généralisation.

---

## 🔧 Étapes de construction (ordre réel)

1. **Setup** — Spring Initializr (Web, WebSocket, Data JPA, PostgreSQL, Validation, DevTools) + ajout manuel de Springdoc OpenAPI ; projet Angular + `@stomp/stompjs` + `sockjs-client`
2. **`WebSocketConfig`** — endpoint `/ws` + SockJS, broker simple `/topic`, préfixe `/app` ; testé via `GET /ws/info` avant tout code métier
3. **Modèle métier** — `MessageType` (enum) → `ChatMessage` (entité JPA immuable) → `ChatMessageRepository` (Query Method) → tests avec `@DataJpaTest`
4. **DTOs + Mapper + Service** — validation (`@NotBlank`), interface/implémentation, injection par constructeur → tests unitaires avec Mockito, sans base ni Spring
5. **Contrôleurs** — `ChatController` (STOMP, `@MessageMapping`/`@SendTo`) et `RoomHistoryController` (REST, documenté Swagger) → test d'intégration avec un vrai client STOMP Java (`WebSocketStompClient`)
6. **`WebSocketEventListener`** — JOIN/LEAVE automatiques via `SessionDisconnectEvent`
7. **Frontend** — modèles typés → `WebSocketConnectionService` → `ChatService` → composants (`RoomSelectorComponent`, `ChatRoomComponent`)
8. **Dockerisation** — Dockerfiles multi-stage, `docker-compose.yml` avec isolation réseau, configuration Nginx spécifique au handshake WebSocket

---

## 🐳 Le point technique le plus important de ce projet — proxifier un handshake WebSocket

Contrairement à un simple `proxy_pass` HTTP, faire passer une connexion WebSocket à travers Nginx exige des lignes explicites :

```nginx
location /ws/ {
    proxy_pass http://backend:8080/ws/;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

**Pourquoi ces lignes sont indispensables** : par défaut, Nginx ne transmet pas les headers `Upgrade`/`Connection` à travers un proxy — sans elles, le handshake `101 Switching Protocols` échoue silencieusement entre Nginx et le backend, même si la connexion initiale client → Nginx semblait fonctionner.

**Même piège rencontré côté développement** : le proxy de `ng serve` (`proxy.conf.json`) ne transmet pas non plus les connexions WebSocket par défaut — il faut explicitement `"ws": true` sur l'entrée concernée.

---

## ✅ Preuves de fonctionnement

```bash
# Le endpoint SockJS répond à travers le reverse proxy
curl http://chat-app.local/ws/info
# → {"entropy":...,"websocket":true,...}

# Isolation réseau — backend injoignable directement
curl http://localhost:8080/ws/info
# → Connection refused

# Handshake visible dans le navigateur : DevTools → Network → filtre "WS"
# → statut 101 Switching Protocols, frames STOMP visibles en direct
```

**Test manuel** : deux onglets ouverts sur le même salon — un message envoyé depuis l'un apparaît instantanément dans l'autre, sans rafraîchissement, avec les notifications JOIN/LEAVE automatiques à la connexion/déconnexion.

**Tests automatisés** :
- `ChatMessageRepositoryTest` (`@DataJpaTest`) — persistance isolée, base H2 en mémoire
- `ChatServiceImplTest` (Mockito) — logique métier isolée, sans base ni Spring
- `ChatIntegrationTest` (`@SpringBootTest`, `WebSocketStompClient`) — un message envoyé est bien reçu par l'abonné du même salon, bout en bout

---

## 📚 Ce que ce projet démontre — points à mettre en avant en entretien

- **Comprendre la différence entre protocoles synchrones et asynchrones/bidirectionnels**, et savoir dire précisément pourquoi HTTP classique ne convient pas à du temps réel (pas juste "c'est plus lent")
- **Savoir lire et expliquer un handshake réseau** (`101 Switching Protocols`) plutôt que d'utiliser une librairie comme une boîte noire
- **Identifier une limite d'architecture avant qu'elle ne devienne un bug en production** : le broker en mémoire ne fonctionne qu'à une seule instance — passer à plusieurs instances nécessiterait un broker externe partagé (RabbitMQ), un point volontairement documenté ici plutôt qu'ignoré
- **Appliquer une séparation des responsabilités cohérente des deux côtés** (DTO/Entité, interface/implémentation, transport/métier) — pas juste côté backend
- **Tester à trois niveaux différents** selon ce qu'on valide (persistance isolée, logique métier isolée avec mocks, intégration bout en bout) plutôt qu'un seul type de test partout
- **Diagnostiquer une erreur réseau précise** (`WebSocket is closed before the connection is established`) en remontant à sa cause exacte (proxy de dev n'gérant pas l'upgrade WebSocket) plutôt que de deviner

---

## 🚧 Pistes d'amélioration identifiées

- Remplacer le broker simple en mémoire par un **broker externe** (RabbitMQ via `enableStompBrokerRelay`) pour supporter plusieurs instances backend en parallèle
- Ajouter une authentification réelle (Keycloak, prochain projet de la roadmap) plutôt qu'un `username` en dur côté frontend
- Ajouter des messages privés (`/user/queue/...`), en plus des salons publics
- HTTPS/WSS devant Nginx, pour chiffrer la connexion WebSocket comme n'importe quelle connexion HTTP sensible
