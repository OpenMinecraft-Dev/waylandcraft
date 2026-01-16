use std::sync::{Arc, Mutex};
use std::ops::DerefMut;
use crate::WLCState;
use smithay::{
    reexports::{
        wayland_server::{
            protocol::{
                wl_data_device_manager::self as wl_ddm,
                wl_data_device_manager::WlDataDeviceManager as WlDDM,
                wl_data_source::{self, WlDataSource},
                wl_data_device::{self, WlDataDevice},
            },
            backend::ClientId,
            DisplayHandle, DataInit, New, GlobalDispatch, Dispatch, Client,
            Resource,
        },
    },
};
use rustix::{
    fd::{AsFd, OwnedFd},
    io::read,
    pipe::{pipe_with, PipeFlags},
};

pub struct WLCDataState {
    pub devices: Vec<WlDataDevice>,
    pub sources: Vec<WlDataSource>,
}

struct WLCDataSourceData {
    mime: Vec<String>,
}

type WLCDataSource = Arc<Mutex<WLCDataSourceData>>;

fn with_source_data<F, R>(source: &WlDataSource, f: F) -> R
    where F: FnOnce(&mut WLCDataSourceData) -> R
{
    let mut guard = source
        .data::<WLCDataSource>()
        .unwrap()
        .lock()
        .unwrap();
    let data = guard.deref_mut();
    f(data)
}

impl WLCDataState {
    pub fn new() -> Self {
        WLCDataState {
            devices: vec![],
            sources: vec![],
        }
    }

    pub fn create_global(&self, disp: &DisplayHandle) {
        disp.create_global::<WLCState, WlDDM, ()>(3, ());
    }
}

impl GlobalDispatch<WlDDM, ()> for WLCState {
    fn bind(
        _state: &mut Self,
        _handle: &DisplayHandle,
        _client: &Client,
        resource: New<WlDDM>,
        _data: &(),
        data_init: &mut DataInit<'_, Self>,
    ) {
        let _ddm: WlDDM = data_init.init(resource, ());
    }
}

impl Dispatch<WlDDM, ()> for WLCState {
    fn request(
        state: &mut Self,
        _client: &Client,
        _ddm: &WlDDM,
        request: wl_ddm::Request,
        _data: &(),
        _disp: &DisplayHandle,
        data_init: &mut DataInit<'_, Self>,
    ) {
        match request {
            wl_ddm::Request::CreateDataSource { id } => {
                let source_data = WLCDataSourceData {
                    mime: vec![],
                };
                let source_data = Arc::new(Mutex::new(source_data));
                let source = data_init.init(id, source_data.clone());

                state.data.sources.push(source);
            },
            wl_ddm::Request::GetDataDevice { id, .. } => {
                let device = data_init.init(id, ());

                state.data.devices.push(device);
            },
            _ => unreachable!(),
        }
    }
}

impl Dispatch<WlDataSource, WLCDataSource> for WLCState {
    fn request(
        _state: &mut Self,
        _client: &Client,
        resource: &WlDataSource,
        request: wl_data_source::Request,
        _source: &WLCDataSource,
        _disp: &DisplayHandle,
        _data_init: &mut DataInit<'_, Self>,
    ) {
        match request {
            wl_data_source::Request::Offer { mime_type } => {
                with_source_data(resource, |data| {
                    data.mime.push(mime_type);
                });
            },
            wl_data_source::Request::Destroy => {},
            wl_data_source::Request::SetActions { .. } => {},
            _ => unreachable!(),
        }
    }

    fn destroyed(
        state: &mut Self,
        _client: ClientId,
        resource: &WlDataSource,
        _data: &WLCDataSource,
    ) {
        state.data.sources.retain(|s| s != resource);
    }
}

fn read_file_descriptor(fd: OwnedFd) -> Vec<u8> {
    let mut data: Vec<u8> = vec![];
    let mut buf: [u8; 16] = [0; 16];
    loop {
        let len = read(&fd, &mut buf).expect("pipe read");
        if len == 0 {
            break;
        }

        data.extend(&buf[..len]);
    }
    data
}

impl Dispatch<WlDataDevice, ()> for WLCState {
    fn request(
        state: &mut Self,
        _client: &Client,
        _device: &WlDataDevice,
        request: wl_data_device::Request,
        _data: &(),
        _disp: &DisplayHandle,
        _data_init: &mut DataInit<'_, Self>,
    ) {
        match request {
            wl_data_device::Request::StartDrag { .. } => {},
            wl_data_device::Request::SetSelection { source, serial: _ } => {
                let text_mime = "text/plain;charset=utf-8";
                if let Some(source) = source {
                    with_source_data(&source, |data| {
                        if !data.mime.iter().any(|s| s == text_mime) {
                            return;
                        }
                        let (read_fd, write_fd) =
                            pipe_with(PipeFlags::CLOEXEC)
                            .expect("pipe open");
                        source.send(text_mime.into(), write_fd.as_fd());
                        state.display_handle.flush_clients().unwrap();
                        drop(write_fd);

                        let data = read_file_descriptor(read_fd);
                        println!(
                            "SELECTION '{}'",
                            String::from_utf8(data).unwrap()
                        );
                    });
                }
            },
            wl_data_device::Request::Release => {},
            _ => unreachable!(),
        }
    }

    fn destroyed(
        state: &mut Self,
        _client: ClientId,
        device: &WlDataDevice,
        _data: &(),
    ) {
        state.data.devices.retain(|d| d != device);
    }
}
