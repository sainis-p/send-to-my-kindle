print("Started Adding the Users.");
db = connect("mongodb://localhost/send_to_kindle");
db.createUser({
  user: "root",
  pwd: "rootpassword",
  roles: [
    {
      role: "readWrite",
      db: "send_to_kindle"
    }
  ]
});
print("Finished Adding the Users.");