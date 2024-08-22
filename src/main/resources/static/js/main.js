"use strict";

const usernamePage = document.querySelector("#username-page");
const chatPage = document.querySelector("#chat-page");
const usernameForm = document.querySelector("#usernameForm");
const messageForm = document.querySelector("#messageForm");
const messageInput = document.querySelector("#message");
const connectingElement = document.querySelector(".connecting");
const chatArea = document.querySelector("#chat-messages");
const logout = document.querySelector("#logout");

let stompClient = null;
let nickname = null;
let fullname = null;
let selectedUserId = null;

function connect(event) {
  nickname = document.querySelector("#nickname").value.trim();
  fullname = document.querySelector("#fullname").value.trim();

  if (nickname && fullname) {
    // Hide username page and show chat page
    usernamePage.classList.add("hidden");
    chatPage.classList.remove("hidden");

    // Create a new SockJS object pointing to the SockJS endpoint
    const socket = new SockJS("/ws");
    // Create a STOMP client object wrapping the SockJS object
    stompClient = Stomp.over(socket);

    // Connect to the STOMP endpoint, and on success, subscribe to the private user queue
    stompClient.connect({}, onConnected, onError);
  }
  event.preventDefault();
}
// Callback function when the user is connected (Websocket connection is established)
function onConnected() {
  stompClient.subscribe(`/user/${nickname}/queue/messages`, onMessageReceived);
  stompClient.subscribe(`/topic/messages`, onGroupMessageReceived);
  stompClient.subscribe(`/topic/sessions`, onSessionUpdate);

  stompClient.send(
    "/app/user.addUser",
    {},
    JSON.stringify({ nickName: nickname, fullName: fullname, status: "ONLINE" })
  );

  document.querySelector("#connected-user-fullname").textContent = fullname;
  findAndDisplayConnectedUsers().then();
}

function onGroupMessageReceived(payload) {
  const message = JSON.parse(payload.body);
  displayMessage(message.senderId, message.content);
}

function onSessionUpdate(payload) {
  const session = JSON.parse(payload.body);
  // Handle session updates (e.g., display session info, start/end timers)
}
// Function to fetch and display list of connected users asynchronously (async because using await)
async function findAndDisplayConnectedUsers() {
  const connectedUsersResponse = await fetch("/users");
  let connectedUsers = await connectedUsersResponse.json();
  // Filter out the current user from the connected users list
  connectedUsers = connectedUsers.filter((user) => user.nickName !== nickname);
  // user.nickName is stored from backend aka await fetch('/users'); and nickname stored from when user logs in
  const connectedUsersList = document.getElementById("connectedUsers");
  connectedUsersList.innerHTML = "";

  // Append each connected user to the list
  connectedUsers.forEach((user) => {
    appendUserElement(user, connectedUsersList);
    // Add separator between users
    if (connectedUsers.indexOf(user) < connectedUsers.length - 1) {
      const separator = document.createElement("li"); // list element
      separator.classList.add("separator");
      connectedUsersList.appendChild(separator);
    }
  });
}

// Function to append a user element to the connected users list
function appendUserElement(user, connectedUsersList) {
  const listItem = document.createElement("li");
  listItem.classList.add("user-item");
  listItem.id = user.nickName;

  const userImage = document.createElement("img");
  userImage.src = "../img/user-icon.png";
  userImage.alt = user.fullName;

  const usernameSpan = document.createElement("span");
  usernameSpan.textContent = user.fullName;

  const receivedMsgs = document.createElement("span");
  receivedMsgs.textContent = "0";
  receivedMsgs.classList.add("nbr-msg", "hidden");

  // Append user image, username, and message count elements to the list item
  listItem.appendChild(userImage);
  listItem.appendChild(usernameSpan);
  listItem.appendChild(receivedMsgs);

  listItem.addEventListener("click", userItemClick);

  connectedUsersList.appendChild(listItem);
}

// Function to handle click on a user item
function userItemClick(event) {
  // Remove active class from all user items
  document.querySelectorAll(".user-item").forEach((item) => {
    item.classList.remove("active");
  });

  // Show message form
  messageForm.classList.remove("hidden");

  // Highlight the clicked user item
  const clickedUser = event.currentTarget;
  clickedUser.classList.add("active");

  // Set selectedUserId to the ID of the clicked user
  selectedUserId = clickedUser.getAttribute("id");
  fetchAndDisplayUserChat().then();

  // Reset message count indicator for the clicked user
  const nbrMsg = clickedUser.querySelector(".nbr-msg");
  nbrMsg.classList.add("hidden");
  nbrMsg.textContent = "0";
}

// Function to display a chat message in the chat area
function displayMessage(senderId, content) {
  const messageContainer = document.createElement("div");
  messageContainer.classList.add("message");

  // Add sender or receiver class based on message sender
  if (senderId === nickname) {
    messageContainer.classList.add("sender");
  } else {
    messageContainer.classList.add("receiver");
  }
  const message = document.createElement("p");
  message.textContent = content;
  // Append message to the message container
  messageContainer.appendChild(message);
  chatArea.appendChild(messageContainer);
}

async function fetchAndDisplayUserChat() {
  const userChatResponse = await fetch(
    `/messages/${nickname}/${selectedUserId}`
  );
  const userChat = await userChatResponse.json();
  // Clear previous messages
  chatArea.innerHTML = "";
  // Display each chat message in the chat area
  if (Array.isArray(userChat)) {
    userChat.forEach((chat) => {
      displayMessage(chat.senderId, chat.content);
    });
  }

  // Scroll to bottom of chat to show latest messages
  chatArea.scrollTop = chatArea.scrollHeight;
}

// Function to handle WebSocket connection error
function onError() {
  connectingElement.textContent =
    "Could not connect to WebSocket server. Please refresh this page to try again!";
  connectingElement.style.color = "red";
}

// Function to send a chat message
function sendMessage(event) {
  const messageContent = messageInput.value.trim();
  if (messageContent && stompClient) {
    // Create a chat message object
    const chatMessage = {
      senderId: nickname,
      recipientId: selectedUserId,
      content: messageInput.value.trim(),
      timestamp: new Date(),
    };

    // Send the chat message to the server via STOMP
    stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
    // Display the sent message in the chat area
    displayMessage(nickname, messageInput.value.trim());
    // Clear message input field after sending
    messageInput.value = "";
  }
  // Scroll chat area to bottom to show latest messages
  chatArea.scrollTop = chatArea.scrollHeight;
  // Prevent form submission
  event.preventDefault();
}

// Function to handle received messages via WebSocket
async function onMessageReceived(payload) {
  // Refresh connected users list
  await findAndDisplayConnectedUsers();
  console.log("Message received", payload);
  // Parse the received message payload
  const message = JSON.parse(payload.body);
  // Display the message in the chat area if the sender matches the selected user
  if (selectedUserId && selectedUserId === message.senderId) {
    displayMessage(message.senderId, message.content);
    chatArea.scrollTop = chatArea.scrollHeight;
  }

  // Highlight the selected user in the connected users list
  if (selectedUserId) {
    document.querySelector(`#${selectedUserId}`).classList.add("active");
  } else {
    messageForm.classList.add("hidden");
  }

  // Display notification for unread messages for the sender
  const notifiedUser = document.querySelector(`#${message.senderId}`);
  if (notifiedUser && !notifiedUser.classList.contains("active")) {
    const nbrMsg = notifiedUser.querySelector(".nbr-msg");
    nbrMsg.classList.remove("hidden");
    nbrMsg.textContent = "";
  }
}

function onLogout() {
  // Send a message to the server to disconnect the user
  stompClient.send(
    "/app/user.disconnectUser",
    {},
    JSON.stringify({
      nickName: nickname,
      fullName: fullname,
      status: "OFFLINE",
    })
  );
  // Reload the page to log out the user
  window.location.reload();
}
// Event listeners for form submission and logout button click
usernameForm.addEventListener("submit", connect, true); // step 1
messageForm.addEventListener("submit", sendMessage, true);
logout.addEventListener("click", onLogout, true);
// Handle logout on window close or refresh
window.onbeforeunload = () => onLogout();
