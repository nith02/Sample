import React from 'react';
import { HashRouter, Route, Switch } from "react-router-dom";
import { context } from './context';
import Main from './views/Main';
import AddShop from './views/AddShop';
import Shop from './views/Shop';
import Camera from './views/Camera';
import AddCamera from './views/AddCamera';
import './App.css';

class App extends React.Component {
    constructor() {
        super();

        this.state = {
            shop_list: [{
                name: 'shopA',
                cameras: ['cameraA1', "cameraA2"],
            },
            {
                name: 'shopB',
                cameras: ['cameraB1', "cameraB2"],
            }]
        }
    }

    onAddShop(shop) {
        this.setState({
            shop_list: this.state.shop_list.concat([{
                name:shop,
                cameras:[],
            }]),
        });
        window.location.hash = '#';
    }

    onAddCamera(shopName, camera) {
        this.state.shop_list.map((shop) => {
            if (shop.name === shopName) {
                shop.cameras = shop.cameras.concat([camera]);
            }
        });

        this.setState(this.state.shop_list);
        window.location.hash = `#shop/${shopName}`;
    }

    render() {
        return (
            <context.Provider
                value={this.state}
            >
                <HashRouter>
                    <Switch>
                        <Route exact path="/" component={Main} />
                        <Route path="/add" render={props => <AddShop onAddShop = {(shop) => {this.onAddShop(shop)}} />} />
                        <Route exact path="/shop/:shop" component={Shop} />
                        <Route path="/shop/:shop/add" render={props => <AddCamera match = {props.match} onAddCamera = {(shopName, camera) => {this.onAddCamera(shopName, camera)}} />} />
                        <Route path="/shop/:shop/camera/:camera" component={Camera} />
                    </Switch>
                </HashRouter>
            </context.Provider>
        )
    }
}

export default App;
