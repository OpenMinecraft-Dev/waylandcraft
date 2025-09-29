use waylandcraft::wlc_init;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let mut instance = wlc_init()?;

    println!("Listening on: '{}'", instance.state.socket.to_str().unwrap());

    loop {
        instance.update();
    }
}
