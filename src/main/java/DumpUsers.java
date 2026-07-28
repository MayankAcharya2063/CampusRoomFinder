import com.example.roomify.model.User;

void main() throws Exception {
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("users.dat"))) {
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) in.readObject();
        for (User u : users) {
            IO.println(u.getUserId() + " | " + u.getName() + " | "
                    + u.getEmail() + " | " + u.getRole() + " | password=" + u.getPassword());
        }
    }
}